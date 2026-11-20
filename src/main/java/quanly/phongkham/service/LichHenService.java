package quanly.phongkham.service;

import quanly.phongkham.dto.ChiTietThuocItem;
import quanly.phongkham.dto.DatLichHenRequest;
import quanly.phongkham.dto.LichSuKhamDTO;
import quanly.phongkham.entity.*;
import quanly.phongkham.repository.*;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class LichHenService {

    // Các trạng thái được tính là "đang chiếm giờ"
    private static final List<String> TRANG_THAI_CHIEM_GIO =
            List.of("ChoDuyet", "DaDuyet", "HoanThanh");

    private static final int THOI_LUONG_PHUT = 30;

    private final LichHenRepository repository;
    private final LichLamViecRepository lichLamViecRepository;
    private final BacSiRepository bacSiRepository;
    private final ChuyenKhoaRepository chuyenKhoaRepository;
    private final BenhNhanRepository benhNhanRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final KetQuaKhamRepository ketQuaKhamRepository;
    private final DonThuocRepository donThuocRepository;
    private final ChiTietDonThuocRepository chiTietDonThuocRepository;

    public LichHenService(
            LichHenRepository repository,
            LichLamViecRepository lichLamViecRepository,
            BacSiRepository bacSiRepository,
            ChuyenKhoaRepository chuyenKhoaRepository,
            BenhNhanRepository benhNhanRepository,
            TaiKhoanRepository taiKhoanRepository,
            KetQuaKhamRepository ketQuaKhamRepository,
            DonThuocRepository donThuocRepository,
            ChiTietDonThuocRepository chiTietDonThuocRepository) {

        this.repository = repository;
        this.lichLamViecRepository = lichLamViecRepository;
        this.bacSiRepository = bacSiRepository;
        this.chuyenKhoaRepository = chuyenKhoaRepository;
        this.benhNhanRepository = benhNhanRepository;
        this.taiKhoanRepository = taiKhoanRepository;
        this.ketQuaKhamRepository = ketQuaKhamRepository;
        this.donThuocRepository = donThuocRepository;
        this.chiTietDonThuocRepository = chiTietDonThuocRepository;
    }

    // =========================
    // CÁC HÀM LẤY LỊCH HẸN
    // =========================

        public List<LichHen> getAll() {
        List<LichHen> danhSach = repository.findAll();

        // Lấy toàn bộ ca làm việc liên quan bằng 1 query (tránh N+1)
        Set<Integer> maLichSet = danhSach.stream()
                .map(LichHen::getMaLich)
                .collect(Collectors.toSet());

        Map<Integer, LocalDate> ngayTheoMaLich = lichLamViecRepository
                .findAllById(maLichSet).stream()
                .collect(Collectors.toMap(
                        LichLamViec::getMaLich,
                        LichLamViec::getNgayLamViec));

        danhSach.forEach(lh ->
                lh.setNgayLamViec(ngayTheoMaLich.get(lh.getMaLich())));

        return danhSach;
    }
    public Optional<LichHen> getById(Integer id) {
        return repository.findById(id);
    }

    public Optional<LichHen> getByMaDatLich(String maDatLich) {
        return repository.findByMaDatLich(maDatLich);
    }

    public List<LichHen> getByBenhNhan(Integer maBenhNhan) {
        return repository.findByMaBenhNhan(maBenhNhan);
    }

    public List<LichHen> getByLich(Integer maLich) {
        return repository.findByMaLich(maLich);
    }

    public List<LichHen> getByTrangThai(String trangThai) {
        return repository.findByTrangThai(trangThai);
    }

    public List<LichHen> getByBenhNhanAndTrangThai(
            Integer maBenhNhan,
            String trangThai) {

        return repository.findByMaBenhNhanAndTrangThai(
                maBenhNhan,
                trangThai
        );
    }

    /**
     * Danh sách giờ bắt đầu đã có người đặt trong một ca,
     * để frontend làm mờ/disable các khung giờ này.
     */
    public List<LocalTime> getGioDaDat(Integer maLich) {
        return repository.findGioDaDat(maLich, TRANG_THAI_CHIEM_GIO);
    }

    // =========================
    // BỆNH NHÂN ĐẶT LỊCH KHÁM
    // =========================

    @Transactional
    public LichHen datLichKham(DatLichHenRequest req) {

        if (req.getMaLich() == null || req.getGioBatDau() == null) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn ca khám và giờ bắt đầu!"
            );
        }

        if (req.getMaBenhNhan() == null) {
            throw new IllegalArgumentException(
                    "Vui lòng đăng nhập tài khoản bệnh nhân để đặt lịch khám!"
            );
        }

        // Chuẩn hóa giờ: bỏ giây và phần lẻ để so sánh chính xác
        LocalTime gioBatDau = req.getGioBatDau().withSecond(0).withNano(0);
        LocalTime gioKetThuc = gioBatDau.plusMinutes(THOI_LUONG_PHUT);

        // Kiểm tra bệnh nhân tồn tại
        BenhNhan benhNhan = benhNhanRepository.findById(req.getMaBenhNhan())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy bệnh nhân!"
                        )
                );

        // Kiểm tra ca làm việc tồn tại
        LichLamViec ca = lichLamViecRepository.findById(req.getMaLich())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Ca làm việc không tồn tại!"
                        )
                );

        // Kiểm tra ca làm việc đang hoạt động
        if (!Boolean.TRUE.equals(ca.getTrangThai())) {
            throw new IllegalArgumentException(
                    "Ca làm việc hiện không hoạt động!"
            );
        }

        // Kiểm tra bác sĩ phụ trách ca làm việc.
        // KHÓA dòng bác sĩ: các request đặt lịch cùng bác sĩ phải xếp hàng,
        // kể cả khi bác sĩ có nhiều ca trong ngày (chống race condition)
        BacSi bacSi = bacSiRepository.findByIdForUpdate(ca.getMaBacSi())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy bác sĩ phụ trách ca khám!"
                        )
                );

        // Kiểm tra bác sĩ đang hoạt động
        if (!Boolean.TRUE.equals(bacSi.getTrangThai())) {
            throw new IllegalArgumentException(
                    "Bác sĩ hiện không hoạt động!"
            );
        }

        // Kiểm tra bác sĩ có chuyên khoa hợp lệ
        if (bacSi.getMaChuyenKhoa() == null
                || chuyenKhoaRepository.findById(
                        bacSi.getMaChuyenKhoa()
                ).isEmpty()) {

            throw new IllegalArgumentException(
                    "Bác sĩ chưa được phân công chuyên khoa!"
            );
        }

        // Kiểm tra ngày làm việc không nằm trong quá khứ
        if (ca.getNgayLamViec().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Không thể đặt lịch vào ngày đã qua!"
            );
        }

        // Kiểm tra thời gian ca làm việc
        if (gioBatDau.isBefore(ca.getGioBatDau())
                || gioKetThuc.isAfter(ca.getGioKetThuc())) {

            throw new IllegalArgumentException(
                    "Giờ khám nằm ngoài thời gian làm việc của bác sĩ!"
            );
        }

        // Không cho đặt giờ đã qua trong ngày hiện tại
        if (ca.getNgayLamViec().isEqual(LocalDate.now())
                && !gioBatDau.isAfter(LocalTime.now())) {

            throw new IllegalArgumentException(
                    "Không thể đặt khung giờ đã qua!"
            );
        }

        // Kiểm tra khung giờ có bị trùng/chồng lấn với lịch đã đặt không
        boolean daDat = repository.existsOverlapTheoBacSiNgay(
                ca.getMaBacSi(),
                ca.getNgayLamViec(),
                gioBatDau,
                gioKetThuc,
                TRANG_THAI_CHIEM_GIO
        );

        if (daDat) {
            throw new IllegalArgumentException(
                    "Khung giờ này đã có bệnh nhân đặt. Vui lòng chọn giờ khác!"
            );
        }

        // Tự lấy tên bệnh nhân từ tài khoản
        String tenBenhNhan = req.getTenBenhNhan();

        if (benhNhan.getMaTaiKhoan() != null) {
            TaiKhoan taiKhoan = taiKhoanRepository
                    .findById(benhNhan.getMaTaiKhoan())
                    .orElse(null);

            if (taiKhoan != null) {
                tenBenhNhan = taiKhoan.getHoTen();
            }
        }

        // Tạo lịch hẹn
        LichHen lichHen = new LichHen();

        lichHen.setMaDatLich(taoMaDatLichDuyNhat());
        lichHen.setMaBenhNhan(req.getMaBenhNhan());
        lichHen.setMaLich(req.getMaLich());

        lichHen.setGioBatDau(gioBatDau);
        lichHen.setGioKetThuc(gioKetThuc);

        lichHen.setLyDoKham(req.getLyDoKham());
        lichHen.setGhiChu(req.getGhiChu());
        lichHen.setTenBenhNhan(tenBenhNhan);

        lichHen.setTrangThai("ChoDuyet");
        lichHen.setNgayDat(LocalDateTime.now());

        // Lớp bảo vệ cuối: unique index ở DB sẽ chặn nếu vẫn lọt trùng
        try {
            return repository.saveAndFlush(lichHen);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(
                    "Khung giờ này vừa có bệnh nhân khác đặt. Vui lòng chọn giờ khác!"
            );
        }
    }

    // =========================
    // TIẾP TÂN DUYỆT LỊCH HẸN
    // =========================

    @Transactional
    public LichHen duyetLichHen(Integer id) {

        LichHen lh = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy lịch hẹn!"
                        )
                );

        if (!"ChoDuyet".equalsIgnoreCase(lh.getTrangThai())) {
            throw new IllegalArgumentException(
                    "Chỉ xác nhận được lịch đang chờ duyệt!"
            );
        }

        lh.setTrangThai("DaDuyet");

        return repository.save(lh);
    }

    // =========================
    // HỦY LỊCH HẸN
    // =========================

    @Transactional
    public LichHen huyLichHen(Integer id, String lyDo) {

        LichHen lh = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy lịch hẹn mã: " + id
                        )
                );

        if ("HoanThanh".equalsIgnoreCase(lh.getTrangThai())) {
            throw new IllegalArgumentException(
                    "Lịch hẹn đã khám xong, không thể hủy!"
            );
        }

        if ("DaHuy".equalsIgnoreCase(lh.getTrangThai())) {
            throw new IllegalArgumentException(
                    "Lịch hẹn này đã được hủy trước đó!"
            );
        }

        lh.setTrangThai("DaHuy");

        if (lyDo != null && !lyDo.isBlank()) {
            String ghiChuCu = lh.getGhiChu() != null
                    ? lh.getGhiChu() + " | "
                    : "";

            lh.setGhiChu(ghiChuCu + "Lý do hủy: " + lyDo);
        }

        return repository.save(lh);
    }

    // =========================
    // ĐỔI TRẠNG THÁI (CÓ KIỂM TRA TRÙNG GIỜ)
    // Controller PATCH /{id}/trang-thai nên gọi hàm này
    // =========================

    @Transactional
    public LichHen capNhatTrangThai(Integer id, String trangThaiMoi) {

        if (trangThaiMoi == null || trangThaiMoi.isBlank()) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ!");
        }

        LichHen lh = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy lịch hẹn mã: " + id
                        )
                );

        boolean dangHuy = "DaHuy".equalsIgnoreCase(lh.getTrangThai());
        boolean moiChiemGio = TRANG_THAI_CHIEM_GIO.contains(trangThaiMoi);

        // Khôi phục lịch đã hủy => phải kiểm tra giờ đó còn trống không
        if (dangHuy && moiChiemGio) {

            LichLamViec ca = lichLamViecRepository.findById(lh.getMaLich())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Ca làm việc không tồn tại!"
                            )
                    );

            // Khóa dòng bác sĩ để tránh đặt trùng cùng lúc
            bacSiRepository.findByIdForUpdate(ca.getMaBacSi())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Không tìm thấy bác sĩ phụ trách ca khám!"
                            )
                    );

            boolean daDat = repository.existsOverlapTheoBacSiNgay(
                    ca.getMaBacSi(),
                    ca.getNgayLamViec(),
                    lh.getGioBatDau(),
                    lh.getGioKetThuc(),
                    TRANG_THAI_CHIEM_GIO
            );

            if (daDat) {
                throw new IllegalArgumentException(
                        "Khung giờ này đã có bệnh nhân khác đặt, không thể khôi phục lịch!"
                );
            }
        }

        lh.setTrangThai(trangThaiMoi);

        try {
            return repository.saveAndFlush(lh);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(
                    "Khung giờ này đã có bệnh nhân khác đặt!"
            );
        }
    }

    // =========================
    // LẤY LỊCH HẸN CỦA BÁC SĨ
    // =========================

    public List<LichHen> getLichHenBacSiTheoNgay(
            Integer maBacSi,
            LocalDate ngay) {

        return repository.findLichHenByBacSiAndNgay(
                maBacSi,
                ngay
        );
    }

    public List<LichHen> getTatCaLichHenCuaBacSi(
            Integer maBacSi) {

        return repository.findAllLichHenByBacSi(maBacSi);
    }

    // =========================
    // LỊCH SỬ KHÁM CỦA BỆNH NHÂN
    // =========================

    public List<LichSuKhamDTO> getLichSuKham(Integer maBenhNhan) {

        List<LichHen> dsLichHen =
                repository.findByMaBenhNhan(maBenhNhan);

        List<LichSuKhamDTO> result = new ArrayList<>();

        for (LichHen lh : dsLichHen) {

            LocalDate ngayKham = null;
            Integer maBacSi = null;
            String tenBacSi = "";
            String tenChuyenKhoa = "";

            if (lh.getMaLich() != null) {

                Optional<LichLamViec> llvOpt =
                        lichLamViecRepository.findById(lh.getMaLich());

                if (llvOpt.isPresent()) {

                    LichLamViec llv = llvOpt.get();

                    ngayKham = llv.getNgayLamViec();
                    maBacSi = llv.getMaBacSi();

                    if (maBacSi != null) {

                        Optional<BacSi> bsOpt =
                                bacSiRepository.findById(maBacSi);

                        if (bsOpt.isPresent()) {

                            BacSi bs = bsOpt.get();
                            tenBacSi = bs.getTenBacSi();

                            if (bs.getMaChuyenKhoa() != null) {

                                Optional<ChuyenKhoa> ckOpt =
                                        chuyenKhoaRepository.findById(
                                                bs.getMaChuyenKhoa()
                                        );

                                if (ckOpt.isPresent()) {
                                    tenChuyenKhoa =
                                            ckOpt.get().getTenChuyenKhoa();
                                }
                            }
                        }
                    }
                }
            }

            Integer maKetQua = null;
            String trieuChung = null;
            String chanDoan = null;
            String loiDan = null;
            Integer maDonThuoc = null;
            String ghiChuDonThuoc = null;

            List<ChiTietThuocItem> danhSachThuoc =
                    new ArrayList<>();

            Optional<KetQuaKham> kqOpt =
                    ketQuaKhamRepository.findByMaLichHen(
                            lh.getMaLichHen()
                    );

            if (kqOpt.isPresent()) {

                KetQuaKham kq = kqOpt.get();

                maKetQua = kq.getMaKetQua();
                trieuChung = kq.getTrieuChung();
                chanDoan = kq.getChanDoan();
                loiDan = kq.getLoiDanBacSi();

                List<DonThuoc> donThuocList =
                        donThuocRepository.findByMaKetQua(maKetQua);

                if (!donThuocList.isEmpty()) {

                    DonThuoc dt = donThuocList.get(0);

                    maDonThuoc = dt.getMaDonThuoc();
                    ghiChuDonThuoc = dt.getGhiChu();

                    List<ChiTietDonThuoc> chiTietList =
                            chiTietDonThuocRepository.findByMaDonThuoc(
                                    maDonThuoc
                            );

                    for (ChiTietDonThuoc ct : chiTietList) {

                        danhSachThuoc.add(
                                ChiTietThuocItem.builder()
                                        .tenThuoc(ct.getTenThuoc())
                                        .soLuong(ct.getSoLuong())
                                        .lieuDung(ct.getLieuDung())
                                        .cachDung(ct.getCachDung())
                                        .build()
                        );
                    }
                }
            }

            result.add(
                    LichSuKhamDTO.builder()
                            .maLichHen(lh.getMaLichHen())
                            .maDatLich(lh.getMaDatLich())
                            .ngayDat(lh.getNgayDat())
                            .ngayKham(ngayKham)
                            .gioBatDau(lh.getGioBatDau())
                            .gioKetThuc(lh.getGioKetThuc())
                            .trangThai(lh.getTrangThai())
                            .lyDoKham(lh.getLyDoKham())
                            .maBacSi(maBacSi)
                            .tenBacSi(tenBacSi)
                            .tenChuyenKhoa(tenChuyenKhoa)
                            .maKetQua(maKetQua)
                            .trieuChung(trieuChung)
                            .chanDoan(chanDoan)
                            .loiDanBacSi(loiDan)
                            .maDonThuoc(maDonThuoc)
                            .ghiChuDonThuoc(ghiChuDonThuoc)
                            .danhSachThuoc(danhSachThuoc)
                            .build()
            );
        }

        return result;
    }

    // =========================
    // SINH MÃ ĐẶT LỊCH DUY NHẤT
    // =========================

    private String taoMaDatLichDuyNhat() {

        String datePrefix = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyMMdd"));

        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

        Random random = new Random();

        for (int attempt = 0; attempt < 100; attempt++) {

            StringBuilder sb = new StringBuilder("PK-");

            sb.append(datePrefix).append("-");

            for (int i = 0; i < 4; i++) {
                sb.append(
                        chars.charAt(random.nextInt(chars.length()))
                );
            }

            String code = sb.toString();

            if (!repository.existsByMaDatLich(code)) {
                return code;
            }
        }

        return "PK-" + System.currentTimeMillis();
    }

    // =========================
    // CRUD
    // =========================

    public LichHen save(LichHen lichHen) {
        return repository.save(lichHen);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }

    public boolean existsByMaDatLich(String maDatLich) {
        return repository.existsByMaDatLich(maDatLich);
    }

   private static final ZoneId ZONE_VN = ZoneId.of("Asia/Ho_Chi_Minh");

@Transactional
public void xoaLichHen(Integer id, boolean laNhanVien) {
    LichHen lh = repository.findById(id)
            .orElseThrow(() ->
                    new IllegalArgumentException("Không tìm thấy lịch hẹn mã: " + id));

    if ("HoanThanh".equalsIgnoreCase(lh.getTrangThai())) {
        throw new IllegalArgumentException(
                "Lịch hẹn đã khám xong, không thể xóa vì còn hồ sơ khám bệnh!");
    }

    // Bệnh nhân chỉ được xóa lịch đã quá hạn khám
    if (!laNhanVien && !laQuaHan(lh)) {
        throw new IllegalArgumentException("Chỉ được xóa lịch hẹn đã quá hạn khám!");
    }

    repository.deleteById(id);
}

private boolean laQuaHan(LichHen lh) {
    if (!"ChoDuyet".equals(lh.getTrangThai()) && !"DaDuyet".equals(lh.getTrangThai())) {
        return false;
    }

    return lichLamViecRepository.findById(lh.getMaLich())
            .map(ca -> LocalDateTime.of(ca.getNgayLamViec(), lh.getGioKetThuc())
                    .isBefore(LocalDateTime.now(ZONE_VN)))
            .orElse(false);
}
}