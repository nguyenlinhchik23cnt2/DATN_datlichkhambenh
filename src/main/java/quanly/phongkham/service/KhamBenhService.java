
package quanly.phongkham.service;

import quanly.phongkham.config.NguoiDungHienTai;
import quanly.phongkham.dto.ChiTietThuocItem;
import quanly.phongkham.dto.KhamBenhRequest;
import quanly.phongkham.entity.*;
import quanly.phongkham.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class KhamBenhService {

    private final LichHenRepository lichHenRepository;
    private final LichLamViecRepository lichLamViecRepository;
    private final KetQuaKhamRepository ketQuaKhamRepository;
    private final DonThuocRepository donThuocRepository;
    private final ChiTietDonThuocRepository chiTietDonThuocRepository;
    private final NguoiDungHienTai nguoiDung;

    public KhamBenhService(
            LichHenRepository lichHenRepository,
            LichLamViecRepository lichLamViecRepository,
            KetQuaKhamRepository ketQuaKhamRepository,
            DonThuocRepository donThuocRepository,
            ChiTietDonThuocRepository chiTietDonThuocRepository,
            NguoiDungHienTai nguoiDung) {

        this.lichHenRepository = lichHenRepository;
        this.lichLamViecRepository = lichLamViecRepository;
        this.ketQuaKhamRepository = ketQuaKhamRepository;
        this.donThuocRepository = donThuocRepository;
        this.chiTietDonThuocRepository = chiTietDonThuocRepository;
        this.nguoiDung = nguoiDung;
    }

    /**
     * Bác sĩ ghi nhận kết quả khám và kê đơn thuốc.
     * Chỉ bác sĩ được phân công hoặc Admin mới được thực hiện.
     */
    @Transactional
    public Map<String, Object> hoanThanhKhamBenh(KhamBenhRequest req) {

        if (req == null || req.getMaLichHen() == null) {
            throw new IllegalArgumentException(
                    "Mã lịch hẹn không được để trống!");
        }

        // 1. Kiểm tra lịch hẹn
        LichHen lichHen = lichHenRepository.findById(req.getMaLichHen())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy lịch hẹn mã: "
                                + req.getMaLichHen()));

        if ("DaHuy".equalsIgnoreCase(lichHen.getTrangThai())) {
            throw new IllegalArgumentException(
                    "Lịch hẹn đã bị hủy, không thể ghi nhận kết quả khám!");
        }

        // 2. Kiểm tra lịch làm việc
        if (lichHen.getMaLich() == null) {
            throw new IllegalArgumentException(
                    "Lịch hẹn chưa được gắn với lịch làm việc!");
        }

        LichLamViec lichLamViec =
                lichLamViecRepository.findById(lichHen.getMaLich())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Không tìm thấy lịch làm việc của lịch hẹn!"));

        // 3. Kiểm tra quyền bác sĩ
        if (!nguoiDung.laAdmin()) {

            if (!nguoiDung.laBacSi()) {
                throw new IllegalArgumentException(
                        "Bạn không có quyền ghi nhận kết quả khám!");
            }

            Integer maBacSiDangNhap =
                    nguoiDung.maBacSi().orElse(null);

            if (maBacSiDangNhap == null
                    || !maBacSiDangNhap.equals(
                            lichLamViec.getMaBacSi())) {

                throw new IllegalArgumentException(
                        "Bạn không được phân công khám lịch hẹn này!");
            }
        }

        // 4. Cập nhật trạng thái lịch hẹn
        lichHen.setTrangThai("HoanThanh");
        lichHenRepository.save(lichHen);

        // 5. Tạo hoặc cập nhật kết quả khám
        KetQuaKham ketQua =
                ketQuaKhamRepository.findByMaLichHen(
                        req.getMaLichHen())
                        .orElse(new KetQuaKham());

        ketQua.setMaLichHen(req.getMaLichHen());
        ketQua.setTrieuChung(req.getTrieuChung());
        ketQua.setChanDoan(req.getChanDoan());
        ketQua.setLoiDanBacSi(req.getLoiDanBacSi());
        ketQua.setNgayKham(LocalDateTime.now());
        ketQua.setTenBenhNhan(lichHen.getTenBenhNhan());

        KetQuaKham savedKq =
                ketQuaKhamRepository.save(ketQua);

        // 6. Tạo hoặc cập nhật đơn thuốc nếu có dữ liệu
        DonThuoc savedDt = null;

        boolean coThuoc =
                req.getDanhSachThuoc() != null
                && !req.getDanhSachThuoc().isEmpty();

        boolean coGhiChu =
                req.getGhiChuDonThuoc() != null
                && !req.getGhiChuDonThuoc().isBlank();

        if (coThuoc || coGhiChu) {

            var existingDtList =
                    donThuocRepository.findByMaKetQua(
                            savedKq.getMaKetQua());

            DonThuoc donThuoc = existingDtList.isEmpty()
                    ? new DonThuoc()
                    : existingDtList.get(0);

            donThuoc.setMaKetQua(savedKq.getMaKetQua());
            donThuoc.setNgayKeDon(LocalDateTime.now());
            donThuoc.setGhiChu(req.getGhiChuDonThuoc());
            donThuoc.setTenBenhNhan(lichHen.getTenBenhNhan());

            savedDt = donThuocRepository.save(donThuoc);

            // Xóa chi tiết thuốc cũ trước khi cập nhật
            var chiTietCu =
                    chiTietDonThuocRepository.findByMaDonThuoc(
                            savedDt.getMaDonThuoc());

            if (!chiTietCu.isEmpty()) {
                chiTietDonThuocRepository.deleteAll(chiTietCu);
                chiTietDonThuocRepository.flush();
            }

            // Lưu danh sách thuốc mới
            if (req.getDanhSachThuoc() != null) {

                for (ChiTietThuocItem item
                        : req.getDanhSachThuoc()) {

                    ChiTietDonThuoc ct =
                            new ChiTietDonThuoc();

                    ct.setMaDonThuoc(
                            savedDt.getMaDonThuoc());

                    ct.setTenThuoc(item.getTenThuoc());

                    ct.setSoLuong(
                            item.getSoLuong() != null
                                    ? item.getSoLuong()
                                    : 1);

                    ct.setLieuDung(item.getLieuDung());
                    ct.setCachDung(item.getCachDung());

                    chiTietDonThuocRepository.save(ct);
                }
            }
        }

        // 7. Trả kết quả
        Map<String, Object> res = new HashMap<>();

        res.put(
                "message",
                "Ghi nhận kết quả khám và đơn thuốc thành công!");

        res.put("maLichHen", lichHen.getMaLichHen());
        res.put("maKetQua", savedKq.getMaKetQua());

        if (savedDt != null) {
            res.put("maDonThuoc", savedDt.getMaDonThuoc());
        }

        return res;
    }
}
