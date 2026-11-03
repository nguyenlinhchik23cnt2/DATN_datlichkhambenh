package quanly.phongkham.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "KetQuaKham")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KetQuaKham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaKetQua")
    private Integer maKetQua;

    @Column(name = "MaLichHen", nullable = false)
    private Integer maLichHen;

    @Column(name = "TrieuChung", length = 1000)
    private String trieuChung;

    @Column(name = "ChanDoan", length = 1000)
    private String chanDoan;

    @Column(name = "LoiDanBacSi", length = 1000)
    private String loiDanBacSi;

    @Column(name = "NgayKham", nullable = false)
    private LocalDateTime ngayKham;

    @Column(name = "TenBenhNhan", length = 100)
    private String tenBenhNhan;
}