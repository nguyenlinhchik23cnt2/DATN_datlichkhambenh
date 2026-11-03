package quanly.phongkham.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "BacSi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BacSi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaBacSi")
    private Integer maBacSi;

    @Column(name = "MaTaiKhoan", nullable = false)
    private Integer maTaiKhoan;

    @Column(name = "MaChuyenKhoa", nullable = false)
    private Integer maChuyenKhoa;

    @Column(name = "HocVi", length = 50)
    private String hocVi;

    @Column(name = "KinhNghiem", length = 255)
    private String kinhNghiem;

    @Column(name = "GioiThieu", length = 500)
    private String gioiThieu;

    @Column(name = "TrangThai", nullable = false)
    private Boolean trangThai = true;

    @Column(name = "TenBacSi", length = 100)
    private String tenBacSi;
}