package quanly.phongkham.service;

import quanly.phongkham.dto.ThongKeChuyenKhoaDTO;
import quanly.phongkham.dto.ThongKeTheoThangDTO;
import quanly.phongkham.dto.ThongKeTongQuanDTO;
import quanly.phongkham.entity.BacSi;
import quanly.phongkham.entity.ChuyenKhoa;
import quanly.phongkham.entity.LichHen;
import quanly.phongkham.entity.LichLamViec;
import quanly.phongkham.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ThongKeService {

    private final BacSiRepository bacSiRepository;
    private final BenhNhanRepository benhNhanRepository;
    private final ChuyenKhoaRepository chuyenKhoaRepository;
    private final LichHenRepository lichHenRepository;
    private final LichLamViecRepository lichLamViecRepository;

    public ThongKeService(
            BacSiRepository bacSiRepository,
            BenhNhanRepository benhNhanRepository,
            ChuyenKhoaRepository chuyenKhoaRepository,
            LichHenRepository lichHenRepository,
            LichLamViecRepository lichLamViecRepository) {
        this.bacSiRepository = bacSiRepository;
        this.benhNhanRepository = benhNhanRepository;
        this.chuyenKhoaRepository = chuyenKhoaRepository;
        this.lichHenRepository = lichHenRepository;
        this.lichLamViecRepository = lichLamViecRepository;
    }

    /**
     * Thong ke tong quan cho Admin Dashboard
     */
    public ThongKeTongQuanDTO getTongQuan() {
        return ThongKeTongQuanDTO.builder()
                .tongBacSi(bacSiRepository.count())
                .tongBenhNhan(benhNhanRepository.count())
                .tongChuyenKhoa(chuyenKhoaRepository.count())
                .tongLichHen(lichHenRepository.count())
                .lichHenChoDuyet(lichHenRepository.countByTrangThai("ChoDuyet"))
                .lichHenDaDuyet(lichHenRepository.countByTrangThai("DaDuyet"))
                .lichHenHoanThanh(lichHenRepository.countByTrangThai("HoanThanh"))
                .lichHenDaHuy(lichHenRepository.countByTrangThai("DaHuy"))
                .build();
    }

    /**
     * Thong ke so luong benh nhan kham theo 12 thang trong nam.
     * Chi tinh cac lich hen da kham xong (HoanThanh) va tinh theo NGAY KHAM
     * (LichLamViec.NgayLamViec), khong phai ngay dat lich.
     */
    public List<ThongKeTheoThangDTO> getThongKeTheoThang(int nam) {
        List<LichHen> allLichHen = lichHenRepository.findAll();

        Map<Integer, LocalDate> maLichToNgayKham = lichLamViecRepository.findAll().stream()
                .filter(l -> l.getMaLich() != null && l.getNgayLamViec() != null)
                .collect(Collectors.toMap(LichLamViec::getMaLich, LichLamViec::getNgayLamViec, (k1, k2) -> k1));

        List<ThongKeTheoThangDTO> result = new ArrayList<>();

        for (int m = 1; m <= 12; m++) {
            final int thang = m;
            long count = allLichHen.stream()
                    .filter(lh -> "HoanThanh".equalsIgnoreCase(lh.getTrangThai()))
                    .map(lh -> maLichToNgayKham.get(lh.getMaLich()))
                    .filter(ngayKham -> ngayKham != null
                            && ngayKham.getYear() == nam
                            && ngayKham.getMonthValue() == thang)
                    .count();

            result.add(ThongKeTheoThangDTO.builder()
                    .thang(thang)
                    .nam(nam)
                    .soLuotKham(count)
                    .build());
        }

        return result;
    }

    /**
     * Thong ke so luong luot kham theo tung Chuyen khoa.
     * Chi tinh cac lich hen da kham xong (HoanThanh).
     */
    public List<ThongKeChuyenKhoaDTO> getThongKeTheoChuyenKhoa(LocalDate tuNgay, LocalDate denNgay) {
        List<ChuyenKhoa> chuyenKhoaList = chuyenKhoaRepository.findAll();
        List<BacSi> bacSiList = bacSiRepository.findAll();
        List<LichLamViec> lichLamViecList = (tuNgay != null && denNgay != null)
                ? lichLamViecRepository.findByNgayLamViecBetween(tuNgay, denNgay)
                : lichLamViecRepository.findAll();

        Map<Integer, Integer> maLichToMaBacSi = lichLamViecList.stream()
                .filter(l -> l.getMaLich() != null && l.getMaBacSi() != null)
                .collect(Collectors.toMap(LichLamViec::getMaLich, LichLamViec::getMaBacSi, (k1, k2) -> k1));

        Map<Integer, Integer> maBacSiToMaChuyenKhoa = bacSiList.stream()
                .filter(b -> b.getMaBacSi() != null && b.getMaChuyenKhoa() != null)
                .collect(Collectors.toMap(BacSi::getMaBacSi, BacSi::getMaChuyenKhoa, (k1, k2) -> k1));

        List<LichHen> allLichHen = lichHenRepository.findAll();

        Map<Integer, Long> countByChuyenKhoa = new HashMap<>();
        for (LichHen lh : allLichHen) {
            if ("HoanThanh".equalsIgnoreCase(lh.getTrangThai())) {
                Integer maBacSi = maLichToMaBacSi.get(lh.getMaLich());
                if (maBacSi != null) {
                    Integer maChuyenKhoa = maBacSiToMaChuyenKhoa.get(maBacSi);
                    if (maChuyenKhoa != null) {
                        countByChuyenKhoa.put(maChuyenKhoa, countByChuyenKhoa.getOrDefault(maChuyenKhoa, 0L) + 1);
                    }
                }
            }
        }

        List<ThongKeChuyenKhoaDTO> result = new ArrayList<>();
        for (ChuyenKhoa ck : chuyenKhoaList) {
            result.add(ThongKeChuyenKhoaDTO.builder()
                    .maChuyenKhoa(ck.getMaChuyenKhoa())
                    .tenChuyenKhoa(ck.getTenChuyenKhoa())
                    .soLuotKham(countByChuyenKhoa.getOrDefault(ck.getMaChuyenKhoa(), 0L))
                    .build());
        }

        return result;
    }
}

