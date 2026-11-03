package quanly.phongkham.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "LichHen")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LichHen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaLichHen")
    private Integer maLichHen;

    @Column(name = "MaDatLich", nullable = false, length = 20)
    private String maDatLich;

    @Column(name = "MaBenhNhan", nullable = false)
    private Integer maBenhNhan;

    @Column(name = "MaLich", nullable = false)
    private Integer maLich;

    @Column(name = "GioBatDau", nullable = false)
    private LocalTime gioBatDau;

    @Column(name = "GioKetThuc", nullable = false)
    private LocalTime gioKetThuc;

    @Column(name = "LyDoKham", length = 500)
    private String lyDoKham;

    @Column(name = "TrangThai", nullable = false, length = 20)
    private String trangThai = "ChoDuyet";

    @Column(name = "NgayDat", nullable = false)
    private LocalDateTime ngayDat;

    @Column(name = "GhiChu", length = 500)
    private String ghiChu;

    @Column(name = "TenBenhNhan", length = 100)
    private String tenBenhNhan;
}