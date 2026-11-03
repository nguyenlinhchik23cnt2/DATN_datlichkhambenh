package quanly.phongkham.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "DonThuoc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DonThuoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDonThuoc")
    private Integer maDonThuoc;

    @Column(name = "MaKetQua", nullable = false)
    private Integer maKetQua;

    @Column(name = "NgayKeDon", nullable = false)
    private LocalDateTime ngayKeDon;

    @Column(name = "GhiChu", length = 500)
    private String ghiChu;

    @Column(name = "TenBenhNhan", length = 100)
    private String tenBenhNhan;
}