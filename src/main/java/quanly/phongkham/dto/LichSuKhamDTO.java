package quanly.phongkham.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LichSuKhamDTO {
    private Integer maLichHen;
    private String maDatLich;
    private LocalDateTime ngayDat;
    private LocalDate ngayKham;
    private LocalTime gioBatDau;
    private LocalTime gioKetThuc;
    private String trangThai;
    private String lyDoKham;

    // Thong tin Bac si & Chuyen khoa
    private Integer maBacSi;
    private String tenBacSi;
    private String tenChuyenKhoa;

    // Ket qua kham
    private Integer maKetQua;
    private String trieuChung;
    private String chanDoan;
    private String loiDanBacSi;

    // Don thuoc
    private Integer maDonThuoc;
    private String ghiChuDonThuoc;
    private List<ChiTietThuocItem> danhSachThuoc;
}

