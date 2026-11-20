package quanly.phongkham.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TaiKhoan", schema = "dbo")
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

    // Nhan mat khau khi tao/cap nhat nhung khong bao gio tra ve trong response
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
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