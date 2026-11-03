package quanly.phongkham.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "LichLamViec")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LichLamViec {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaLich")
    private Integer maLich;

    @Column(name = "MaBacSi", nullable = false)
    private Integer maBacSi;

    @Column(name = "NgayLamViec", nullable = false)
    private LocalDate ngayLamViec;

    @Column(name = "GioBatDau", nullable = false)
    private LocalTime gioBatDau;

    @Column(name = "GioKetThuc", nullable = false)
    private LocalTime gioKetThuc;

    @Column(name = "TrangThai", nullable = false)
    private Boolean trangThai = true;

    @Column(name = "TenBacSi", length = 100)
    private String tenBacSi;
}