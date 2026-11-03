package quanly.phongkham.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TuVanAI")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TuVanAI {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaTuVan")
    private Integer maTuVan;

    @Column(name = "MaBenhNhan")
    private Integer maBenhNhan;

    @Column(name = "NoiDungTrieuChung", nullable = false, length = 2000)
    private String noiDungTrieuChung;

    @Column(name = "NhanDinhAI", columnDefinition = "nvarchar(max)")
    private String nhanDinhAI;

    @Column(name = "MaChuyenKhoaGoiY")
    private Integer maChuyenKhoaGoiY;

    @Column(name = "ThoiGianTuVan", nullable = false)
    private LocalDateTime thoiGianTuVan;
}