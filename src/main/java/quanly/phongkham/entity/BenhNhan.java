package quanly.phongkham.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "BenhNhan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BenhNhan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaBenhNhan")
    private Integer maBenhNhan;

    @Column(name = "MaTaiKhoan", nullable = false)
    private Integer maTaiKhoan;

    @Column(name = "NgaySinh")
    private LocalDate ngaySinh;

    @Column(name = "GioiTinh", length = 10)
    private String gioiTinh;

    @Column(name = "DiaChi", length = 255)
    private String diaChi;
}