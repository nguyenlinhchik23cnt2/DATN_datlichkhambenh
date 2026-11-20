package quanly.phongkham.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import quanly.phongkham.config.NguoiDungHienTai;
import quanly.phongkham.dto.ChiTietThuocItem;
import quanly.phongkham.dto.DatLichHenRequest;
import quanly.phongkham.dto.KhamBenhRequest;
import quanly.phongkham.dto.KhungGioSlotDTO;
import quanly.phongkham.dto.LichSuKhamDTO;
import quanly.phongkham.entity.*;
import quanly.phongkham.repository.*;

import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class KhamBenhVaLichHenTest {

    private final Map<Integer, LichLamViec> dbLichLamViec = new HashMap<>();
    private final Map<Integer, LichHen> dbLichHen = new HashMap<>();
    private final Map<Integer, KetQuaKham> dbKetQuaKham = new HashMap<>();
    private final Map<Integer, DonThuoc> dbDonThuoc = new HashMap<>();
    private final Map<Integer, ChiTietDonThuoc> dbChiTietDonThuoc = new HashMap<>();
    private final Map<Integer, BacSi> dbBacSi = new HashMap<>();
    private final Map<Integer, ChuyenKhoa> dbChuyenKhoa = new HashMap<>();
    private final Map<Integer, BenhNhan> dbBenhNhan = new HashMap<>();
    private final Map<Integer, TaiKhoan> dbTaiKhoan = new HashMap<>();

    private int idSeqLlv = 1;
    private int idSeqLh = 1;
    private int idSeqKq = 1;
    private int idSeqDt = 1;
    private int idSeqCt = 1;

    // Ngày khám dùng cho các test đặt lịch (luôn nằm trong tương lai)
    private final LocalDate ngayMai = LocalDate.now().plusDays(1);

    private LichLamViecService lichLamViecService;
    private LichHenService lichHenService;
    private KhamBenhService khamBenhService;

    // Giờ kết thúc của lịch hẹn; nếu chưa set thì mặc định 30 phút
    private static LocalTime ketThucCuaLichHen(LichHen l) {
        return l.getGioKetThuc() != null
                ? l.getGioKetThuc()
                : l.getGioBatDau().plusMinutes(30);
    }

    @BeforeEach
    void setUp() {
        dbLichLamViec.clear();
        dbLichHen.clear();
        dbKetQuaKham.clear();
        dbDonThuoc.clear();
        dbChiTietDonThuoc.clear();
        dbBacSi.clear();
        dbChuyenKhoa.clear();
        dbBenhNhan.clear();
        dbTaiKhoan.clear();

        idSeqLlv = 1;
        idSeqLh = 1;
        idSeqKq = 1;
        idSeqDt = 1;
        idSeqCt = 1;

        LichLamViecRepository mockLlvRepo = (LichLamViecRepository) Proxy.newProxyInstance(
                LichLamViecRepository.class.getClassLoader(),
                new Class<?>[]{LichLamViecRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("findById".equals(name)) return Optional.ofNullable(dbLichLamViec.get((Integer) args[0]));
                    if ("save".equals(name)) {
                        LichLamViec l = (LichLamViec) args[0];
                        if (l.getMaLich() == null) l.setMaLich(idSeqLlv++);
                        dbLichLamViec.put(l.getMaLich(), l);
                        return l;
                    }
                    if ("saveAll".equals(name)) {
                        List<LichLamViec> saved = new ArrayList<>();
                        for (Object o : (Iterable<?>) args[0]) {
                            LichLamViec l = (LichLamViec) o;
                            if (l.getMaLich() == null) l.setMaLich(idSeqLlv++);
                            dbLichLamViec.put(l.getMaLich(), l);
                            saved.add(l);
                        }
                        return saved;
                    }
                    if ("findByMaBacSiAndNgayLamViec".equals(name)) {
                        Integer bsId = (Integer) args[0];
                        LocalDate date = (LocalDate) args[1];
                        return dbLichLamViec.values().stream()
                                .filter(l -> bsId.equals(l.getMaBacSi()) && date.equals(l.getNgayLamViec()))
                                .toList();
                    }
                    if ("findByMaBacSiAndNgayLamViecAndTrangThaiTrue".equals(name)) {
                        Integer bsId = (Integer) args[0];
                        LocalDate date = (LocalDate) args[1];
                        return dbLichLamViec.values().stream()
                                .filter(l -> bsId.equals(l.getMaBacSi()) && date.equals(l.getNgayLamViec()) && Boolean.TRUE.equals(l.getTrangThai()))
                                .toList();
                    }
                    return null;
                }
        );

        LichHenRepository mockLhRepo = (LichHenRepository) Proxy.newProxyInstance(
                LichHenRepository.class.getClassLoader(),
                new Class<?>[]{LichHenRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("save".equals(name) || "saveAndFlush".equals(name)) {
                        LichHen lh = (LichHen) args[0];
                        if (lh.getMaLichHen() == null) lh.setMaLichHen(idSeqLh++);
                        dbLichHen.put(lh.getMaLichHen(), lh);
                        return lh;
                    }
                    if ("findById".equals(name)) return Optional.ofNullable(dbLichHen.get((Integer) args[0]));
                    if ("findByMaBenhNhan".equals(name)) {
                        Integer bnId = (Integer) args[0];
                        return dbLichHen.values().stream().filter(l -> bnId.equals(l.getMaBenhNhan())).toList();
                    }
                    if ("existsByMaLichAndGioBatDauAndTrangThaiIn".equals(name)) {
                        Integer maLich = (Integer) args[0];
                        LocalTime gio = (LocalTime) args[1];
                        List<?> statusList = (List<?>) args[2];
                        return dbLichHen.values().stream()
                                .anyMatch(l -> maLich.equals(l.getMaLich())
                                        && gio.equals(l.getGioBatDau())
                                        && statusList.contains(l.getTrangThai()));
                    }
                    // Kiểm tra chồng lấn theo bác sĩ + ngày (mọi ca của bác sĩ trong ngày)
                    if ("existsOverlapTheoBacSiNgay".equals(name)) {
                        Integer maBacSi = (Integer) args[0];
                        LocalDate ngay = (LocalDate) args[1];
                        LocalTime batDau = (LocalTime) args[2];
                        LocalTime ketThuc = (LocalTime) args[3];
                        List<?> statusList = (List<?>) args[4];

                        Set<Integer> maLichCuaBacSi = new HashSet<>();
                        for (LichLamViec l : dbLichLamViec.values()) {
                            if (maBacSi.equals(l.getMaBacSi()) && ngay.equals(l.getNgayLamViec())) {
                                maLichCuaBacSi.add(l.getMaLich());
                            }
                        }

                        return dbLichHen.values().stream()
                                .anyMatch(l -> maLichCuaBacSi.contains(l.getMaLich())
                                        && statusList.contains(l.getTrangThai())
                                        && l.getGioBatDau() != null
                                        && l.getGioBatDau().isBefore(ketThuc)
                                        && ketThucCuaLichHen(l).isAfter(batDau));
                    }
                    if ("existsByMaDatLich".equals(name)) {
                        return dbLichHen.values().stream().anyMatch(l -> args[0].equals(l.getMaDatLich()));
                    }
                    return null;
                }
        );

        KetQuaKhamRepository mockKqRepo = (KetQuaKhamRepository) Proxy.newProxyInstance(
                KetQuaKhamRepository.class.getClassLoader(),
                new Class<?>[]{KetQuaKhamRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("save".equals(name)) {
                        KetQuaKham kq = (KetQuaKham) args[0];
                        if (kq.getMaKetQua() == null) kq.setMaKetQua(idSeqKq++);
                        dbKetQuaKham.put(kq.getMaKetQua(), kq);
                        return kq;
                    }
                    if ("findByMaLichHen".equals(name)) {
                        Integer lhId = (Integer) args[0];
                        return dbKetQuaKham.values().stream().filter(k -> lhId.equals(k.getMaLichHen())).findFirst();
                    }
                    return null;
                }
        );

        DonThuocRepository mockDtRepo = (DonThuocRepository) Proxy.newProxyInstance(
                DonThuocRepository.class.getClassLoader(),
                new Class<?>[]{DonThuocRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("save".equals(name)) {
                        DonThuoc dt = (DonThuoc) args[0];
                        if (dt.getMaDonThuoc() == null) dt.setMaDonThuoc(idSeqDt++);
                        dbDonThuoc.put(dt.getMaDonThuoc(), dt);
                        return dt;
                    }
                    if ("findByMaKetQua".equals(name)) {
                        Integer kqId = (Integer) args[0];
                        return dbDonThuoc.values().stream().filter(d -> kqId.equals(d.getMaKetQua())).toList();
                    }
                    return null;
                }
        );

        ChiTietDonThuocRepository mockCtRepo = (ChiTietDonThuocRepository) Proxy.newProxyInstance(
                ChiTietDonThuocRepository.class.getClassLoader(),
                new Class<?>[]{ChiTietDonThuocRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("save".equals(name)) {
                        ChiTietDonThuoc ct = (ChiTietDonThuoc) args[0];
                        if (ct.getMaChiTiet() == null) ct.setMaChiTiet(idSeqCt++);
                        dbChiTietDonThuoc.put(ct.getMaChiTiet(), ct);
                        return ct;
                    }
                    if ("findByMaDonThuoc".equals(name)) {
                        Integer dtId = (Integer) args[0];
                        return dbChiTietDonThuoc.values().stream().filter(c -> dtId.equals(c.getMaDonThuoc())).toList();
                    }
                    if ("deleteAll".equals(name)) {
                        Iterable<?> items = (Iterable<?>) args[0];
                        for (Object item : items) {
                            dbChiTietDonThuoc.remove(((ChiTietDonThuoc) item).getMaChiTiet());
                        }
                        return null;
                    }
                    return null;
                }
        );

        BacSiRepository mockBsRepo = (BacSiRepository) Proxy.newProxyInstance(
                BacSiRepository.class.getClassLoader(),
                new Class<?>[]{BacSiRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    // findByIdForUpdate hoạt động như findById trong test (không cần khóa thật)
                    if ("findById".equals(name) || "findByIdForUpdate".equals(name)) {
                        return Optional.ofNullable(dbBacSi.get((Integer) args[0]));
                    }
                    return null;
                }
        );

        ChuyenKhoaRepository mockCkRepo = (ChuyenKhoaRepository) Proxy.newProxyInstance(
                ChuyenKhoaRepository.class.getClassLoader(),
                new Class<?>[]{ChuyenKhoaRepository.class},
                (proxy, method, args) -> {
                    if ("findById".equals(method.getName())) return Optional.ofNullable(dbChuyenKhoa.get((Integer) args[0]));
                    return null;
                }
        );

        BenhNhanRepository mockBnRepo = (BenhNhanRepository) Proxy.newProxyInstance(
                BenhNhanRepository.class.getClassLoader(),
                new Class<?>[]{BenhNhanRepository.class},
                (proxy, method, args) -> {
                    if ("findById".equals(method.getName())) return Optional.ofNullable(dbBenhNhan.get((Integer) args[0]));
                    return null;
                }
        );

        TaiKhoanRepository mockTkRepo = (TaiKhoanRepository) Proxy.newProxyInstance(
                TaiKhoanRepository.class.getClassLoader(),
                new Class<?>[]{TaiKhoanRepository.class},
                (proxy, method, args) -> {
                    if ("findById".equals(method.getName())) return Optional.ofNullable(dbTaiKhoan.get((Integer) args[0]));
                    return null;
                }
        );

        // LichLamViecService nhận thêm BacSiRepository (khóa bác sĩ khi tạo ca mặc định)
        lichLamViecService = new LichLamViecService(mockLlvRepo, mockLhRepo, mockBsRepo);

        lichHenService = new LichHenService(
                mockLhRepo, mockLlvRepo, mockBsRepo, mockCkRepo,
                mockBnRepo, mockTkRepo, mockKqRepo, mockDtRepo, mockCtRepo
        );

        NguoiDungHienTai mockNguoiDung =
                org.mockito.Mockito.mock(NguoiDungHienTai.class);

        org.mockito.Mockito.when(mockNguoiDung.laAdmin())
                .thenReturn(true);

        khamBenhService = new KhamBenhService(
                mockLhRepo,
                mockLlvRepo,
                mockKqRepo,
                mockDtRepo,
                mockCtRepo,
                mockNguoiDung
        );
    }

    // Dữ liệu nền cho các test đặt lịch: bệnh nhân, bác sĩ đang hoạt động, chuyên khoa
    private void chuanBiBacSiVaBenhNhan() {
        dbBenhNhan.put(201, new BenhNhan());
        dbBenhNhan.put(202, new BenhNhan());

        dbChuyenKhoa.put(1, new ChuyenKhoa(1, "Tai Mũi Họng", "Khám TMH", true));

        BacSi bs = new BacSi();
        bs.setMaBacSi(101);
        bs.setTenBacSi("BS Lan");
        bs.setMaChuyenKhoa(1);
        bs.setTrangThai(true);
        dbBacSi.put(101, bs);
    }

    @Test
    @DisplayName("Bệnh nhân: Chia và kiểm tra slot 30 phút còn trống trong ca làm việc")
    void testGetAvailableSlots() {
        chuanBiBacSiVaBenhNhan();

        // Ca lam viec tu 08:00 den 09:30 (tong cong 3 slots: 08:00-08:30, 08:30-09:00, 09:00-09:30)
        LichLamViec ca = new LichLamViec(1, 101, ngayMai, LocalTime.of(8, 0), LocalTime.of(9, 30), true, "BS Lan");
        dbLichLamViec.put(1, ca);

        // Slot 08:00 da co nguoi dat
        LichHen daDat = new LichHen();
        daDat.setMaLich(1);
        daDat.setGioBatDau(LocalTime.of(8, 0));
        daDat.setGioKetThuc(LocalTime.of(8, 30));
        daDat.setTrangThai("DaDuyet");
        dbLichHen.put(1, daDat);

        List<KhungGioSlotDTO> slots = lichLamViecService.getAvailableSlots(101, ngayMai);

        assertEquals(3, slots.size());
        assertFalse(slots.get(0).getConTrong()); // Slot 08:00 - da dat
        assertTrue(slots.get(1).getConTrong());  // Slot 08:30 - con trong
        assertTrue(slots.get(2).getConTrong());  // Slot 09:00 - con trong
    }

    @Test
    @DisplayName("Bệnh nhân: Slot của lịch đã hoàn thành cũng không còn trống")
    void testSlotHoanThanhKhongConTrong() {
        chuanBiBacSiVaBenhNhan();

        dbLichLamViec.put(1, new LichLamViec(1, 101, ngayMai, LocalTime.of(8, 0), LocalTime.of(9, 0), true, "BS Lan"));

        LichHen xong = new LichHen();
        xong.setMaLich(1);
        xong.setGioBatDau(LocalTime.of(8, 0));
        xong.setGioKetThuc(LocalTime.of(8, 30));
        xong.setTrangThai("HoanThanh");
        dbLichHen.put(1, xong);

        List<KhungGioSlotDTO> slots = lichLamViecService.getAvailableSlots(101, ngayMai);

        assertEquals(2, slots.size());
        assertFalse(slots.get(0).getConTrong());
        assertTrue(slots.get(1).getConTrong());
    }

    @Test
    @DisplayName("Bệnh nhân: Bác sĩ chưa có ca thì tự tạo đúng 2 ca mặc định, gọi lại không tạo thêm")
    void testTuTaoCaMacDinhMotLan() {
        chuanBiBacSiVaBenhNhan();

        List<KhungGioSlotDTO> lan1 = lichLamViecService.getAvailableSlots(101, ngayMai);
        List<KhungGioSlotDTO> lan2 = lichLamViecService.getAvailableSlots(101, ngayMai);

        assertEquals(2, dbLichLamViec.size()); // ca sáng + ca chiều, không bị nhân đôi
        assertEquals(14, lan1.size());          // 7 slot sáng + 7 slot chiều
        assertEquals(14, lan2.size());
    }

    @Test
    @DisplayName("Bệnh nhân: Đặt lịch khám nhận mã đặt lịch và slot 30 phút")
    void testDatLichKhamSuccess() {
        chuanBiBacSiVaBenhNhan();

        LichLamViec ca = new LichLamViec(1, 101, ngayMai, LocalTime.of(8, 0), LocalTime.of(12, 0), true, "BS Lan");
        dbLichLamViec.put(1, ca);

        DatLichHenRequest req = DatLichHenRequest.builder()
                .maBenhNhan(201)
                .maLich(1)
                .gioBatDau(LocalTime.of(8, 30))
                .lyDoKham("Đau đầu và sốt nhẹ")
                .tenBenhNhan("Bệnh nhân An")
                .build();

        LichHen lh = lichHenService.datLichKham(req);

        assertNotNull(lh.getMaLichHen());
        assertTrue(lh.getMaDatLich().startsWith("PK-"));
        assertEquals(LocalTime.of(8, 30), lh.getGioBatDau());
        assertEquals(LocalTime.of(9, 0), lh.getGioKetThuc()); // Dung 30 phut
        assertEquals("ChoDuyet", lh.getTrangThai());
    }

    @Test
    @DisplayName("Bệnh nhân: Đặt lịch trùng slot đã có người đặt phải báo lỗi")
    void testDatLichTrungSlotFail() {
        chuanBiBacSiVaBenhNhan();

        LichLamViec ca = new LichLamViec(1, 101, ngayMai, LocalTime.of(8, 0), LocalTime.of(12, 0), true, "BS Lan");
        dbLichLamViec.put(1, ca);

        LichHen daDat = new LichHen();
        daDat.setMaLich(1);
        daDat.setGioBatDau(LocalTime.of(8, 30));
        daDat.setGioKetThuc(LocalTime.of(9, 0));
        daDat.setTrangThai("ChoDuyet");
        dbLichHen.put(1, daDat);

        DatLichHenRequest req = DatLichHenRequest.builder()
                .maBenhNhan(202)
                .maLich(1)
                .gioBatDau(LocalTime.of(8, 30))
                .lyDoKham("Khám tổng quát")
                .build();

        Exception ex = assertThrows(IllegalArgumentException.class, () -> lichHenService.datLichKham(req));
        assertTrue(ex.getMessage().contains("đã có bệnh nhân đặt"));
    }

    @Test
    @DisplayName("Bệnh nhân: Đặt giờ chồng lên slot đã có người (08:15 đè 08:30) phải báo lỗi")
    void testDatLichChongLanFail() {
        chuanBiBacSiVaBenhNhan();

        dbLichLamViec.put(1, new LichLamViec(1, 101, ngayMai, LocalTime.of(8, 0), LocalTime.of(12, 0), true, "BS Lan"));

        LichHen daDat = new LichHen();
        daDat.setMaLich(1);
        daDat.setGioBatDau(LocalTime.of(8, 30));
        daDat.setGioKetThuc(LocalTime.of(9, 0));
        daDat.setTrangThai("DaDuyet");
        dbLichHen.put(1, daDat);

        DatLichHenRequest req = DatLichHenRequest.builder()
                .maBenhNhan(202)
                .maLich(1)
                .gioBatDau(LocalTime.of(8, 15))
                .lyDoKham("Khám tổng quát")
                .build();

        assertThrows(IllegalArgumentException.class, () -> lichHenService.datLichKham(req));
    }

    @Test
    @DisplayName("Bệnh nhân: Hai ca trùng giờ của cùng bác sĩ trong ngày vẫn không cho đặt trùng giờ")
    void testDatLichTrungGioKhacCaCungBacSiFail() {
        chuanBiBacSiVaBenhNhan();

        // Cùng bác sĩ, cùng ngày, cùng giờ nhưng MaLich khác nhau (tình huống lỗi ban đầu)
        dbLichLamViec.put(1, new LichLamViec(1, 101, ngayMai, LocalTime.of(8, 0), LocalTime.of(12, 0), true, "BS Lan"));
        dbLichLamViec.put(2, new LichLamViec(2, 101, ngayMai, LocalTime.of(8, 0), LocalTime.of(12, 0), true, "BS Lan"));

        DatLichHenRequest nguoiA = DatLichHenRequest.builder()
                .maBenhNhan(201).maLich(1).gioBatDau(LocalTime.of(8, 0))
                .lyDoKham("Khám A").build();
        lichHenService.datLichKham(nguoiA);

        DatLichHenRequest nguoiB = DatLichHenRequest.builder()
                .maBenhNhan(202).maLich(2).gioBatDau(LocalTime.of(8, 0))
                .lyDoKham("Khám B").build();

        Exception ex = assertThrows(IllegalArgumentException.class, () -> lichHenService.datLichKham(nguoiB));
        assertTrue(ex.getMessage().contains("đã có bệnh nhân đặt"));
    }

    @Test
    @DisplayName("Bệnh nhân: Lịch đã hủy thì giờ đó đặt lại được")
    void testDatLaiGioDaHuy() {
        chuanBiBacSiVaBenhNhan();

        dbLichLamViec.put(1, new LichLamViec(1, 101, ngayMai, LocalTime.of(8, 0), LocalTime.of(12, 0), true, "BS Lan"));

        LichHen daHuy = new LichHen();
        daHuy.setMaLich(1);
        daHuy.setGioBatDau(LocalTime.of(8, 30));
        daHuy.setGioKetThuc(LocalTime.of(9, 0));
        daHuy.setTrangThai("DaHuy");
        dbLichHen.put(1, daHuy);

        DatLichHenRequest req = DatLichHenRequest.builder()
                .maBenhNhan(202).maLich(1).gioBatDau(LocalTime.of(8, 30))
                .lyDoKham("Khám lại").build();

        LichHen lh = lichHenService.datLichKham(req);
        assertEquals("ChoDuyet", lh.getTrangThai());
    }

    @Test
    @DisplayName("Bác sĩ / Tiếp tân: Duyệt và Hủy lịch hẹn")
    void testDuyetVaHuyLichHen() {
        LichHen lh = new LichHen();
        lh.setMaLichHen(1);
        lh.setTrangThai("ChoDuyet");
        dbLichHen.put(1, lh);

        LichHen daDuyet = lichHenService.duyetLichHen(1);
        assertEquals("DaDuyet", daDuyet.getTrangThai());

        LichHen daHuy = lichHenService.huyLichHen(1, "Bệnh nhân báo bận đột xuất");
        assertEquals("DaHuy", daHuy.getTrangThai());
        assertTrue(daHuy.getGhiChu().contains("Bệnh nhân báo bận"));
    }

    @Test
    @DisplayName("Bác sĩ: Khám bệnh trọn gói (ghi chẩn đoán, hoàn thành khám, kê đơn thuốc)")
    void testHoanThanhKhamBenh() {
        LichHen lh = new LichHen();
        lh.setMaLichHen(1);
        lh.setTrangThai("DaDuyet");
        lh.setTenBenhNhan("Bệnh nhân An");
        dbLichHen.put(1, lh);

        KhamBenhRequest req = KhamBenhRequest.builder()
                .maLichHen(1)
                .trieuChung("Đau họng, ho có đờm")
                .chanDoan("Viêm họng cấp tính")
                .loiDanBacSi("Uống nhiều nước ấm, tránh uống nước đá")
                .ghiChuDonThuoc("Uống sau bữa ăn")
                .danhSachThuoc(List.of(
                        new ChiTietThuocItem("Paracetamol 500mg", 10, "1 viên/lần", "Ngày uống 2 lần sau ăn"),
                        new ChiTietThuocItem("Amoxicillin 500mg", 14, "1 viên/lần", "Sáng 1 viên, tối 1 viên")
                ))
                .build();

        var res = khamBenhService.hoanThanhKhamBenh(req);

        assertEquals("HoanThanh", dbLichHen.get(1).getTrangThai());
        assertEquals(1, dbKetQuaKham.size());
        assertEquals(1, dbDonThuoc.size());
        assertEquals(2, dbChiTietDonThuoc.size());
        assertNotNull(res.get("maKetQua"));
        assertNotNull(res.get("maDonThuoc"));
    }

    @Test
    @DisplayName("Bệnh nhân: Xem lại lịch sử các lần khám kèm đơn thuốc")
    void testGetLichSuKham() {
        // Bac si & Chuyen khoa
        ChuyenKhoa ck = new ChuyenKhoa(1, "Tai Mũi Họng", "Khám TMH", true);
        dbChuyenKhoa.put(1, ck);

        BacSi bs = new BacSi();
        bs.setMaBacSi(101);
        bs.setTenBacSi("BS Lan");
        bs.setMaChuyenKhoa(1);
        dbBacSi.put(101, bs);

        // Ca lam viec
        LichLamViec ca = new LichLamViec(1, 101, LocalDate.of(2026, 11, 1), LocalTime.of(8, 0), LocalTime.of(11, 30), true, "BS Lan");
        dbLichLamViec.put(1, ca);

        // Lich hen da hoan thanh
        LichHen lh = new LichHen();
        lh.setMaLichHen(1);
        lh.setMaDatLich("PK-261101-ABCD");
        lh.setMaBenhNhan(201);
        lh.setMaLich(1);
        lh.setGioBatDau(LocalTime.of(8, 0));
        lh.setGioKetThuc(LocalTime.of(8, 30));
        lh.setTrangThai("HoanThanh");
        dbLichHen.put(1, lh);

        // Ket qua kham & don thuoc
        KetQuaKham kq = new KetQuaKham();
        kq.setMaKetQua(1);
        kq.setMaLichHen(1);
        kq.setChanDoan("Viêm amidan");
        dbKetQuaKham.put(1, kq);

        DonThuoc dt = new DonThuoc();
        dt.setMaDonThuoc(1);
        dt.setMaKetQua(1);
        dbDonThuoc.put(1, dt);

        ChiTietDonThuoc ct = new ChiTietDonThuoc(1, 1, "Thuốc A", 5, "1 viên/ngày", "Uống sau ăn");
        dbChiTietDonThuoc.put(1, ct);

        List<LichSuKhamDTO> lichSu = lichHenService.getLichSuKham(201);

        assertEquals(1, lichSu.size());
        LichSuKhamDTO item = lichSu.get(0);
        assertEquals("PK-261101-ABCD", item.getMaDatLich());
        assertEquals("BS Lan", item.getTenBacSi());
        assertEquals("Tai Mũi Họng", item.getTenChuyenKhoa());
        assertEquals("Viêm amidan", item.getChanDoan());
        assertEquals(1, item.getDanhSachThuoc().size());
        assertEquals("Thuốc A", item.getDanhSachThuoc().get(0).getTenThuoc());
    }
}