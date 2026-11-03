package quanly.phongkham.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TaiKhoan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaiKhoan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaTaiKhoan")
    private Integer maTaiKhoan;

    @Column(name = "TenDangNhap", nullable = false, length = 50)
    private String tenDangNhap;

    @Column(name = "MatKhau", nullable = false, length = 255)
    private String matKhau;

    @Column(name = "HoTen", nullable = false, length = 100)
    private String hoTen;

    @Column(name = "Email", length = 100)
    private String email;

    @Column(name = "SoDienThoai", length = 15)
    private String soDienThoai;

    @Column(name = "VaiTro", nullable = false, length = 20)
    private String vaiTro;

    @Column(name = "TrangThai", nullable = false)
    private Boolean trangThai = true;

    @Column(name = "NgayTao", nullable = false)
    private LocalDateTime ngayTao;
}