package quanly.phongkham.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ChiTietDonThuoc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietDonThuoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaChiTiet")
    private Integer maChiTiet;

    @Column(name = "MaDonThuoc", nullable = false)
    private Integer maDonThuoc;

    @Column(name = "TenThuoc", nullable = false, length = 150)
    private String tenThuoc;

    @Column(name = "SoLuong", nullable = false)
    private Integer soLuong;

    @Column(name = "LieuDung", length = 255)
    private String lieuDung;

    @Column(name = "CachDung", length = 500)
    private String cachDung;
}