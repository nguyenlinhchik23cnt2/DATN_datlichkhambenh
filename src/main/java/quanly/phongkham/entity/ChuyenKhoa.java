package quanly.phongkham.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ChuyenKhoa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChuyenKhoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaChuyenKhoa")
    private Integer maChuyenKhoa;

    @Column(name = "TenChuyenKhoa", nullable = false, length = 100)
    private String tenChuyenKhoa;

    @Column(name = "MoTa", length = 500)
    private String moTa;

    @Column(name = "TrangThai", nullable = false)
    private Boolean trangThai = true;
}