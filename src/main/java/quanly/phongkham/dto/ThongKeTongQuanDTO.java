package quanly.phongkham.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThongKeTongQuanDTO {
    private long tongBacSi;
    private long tongBenhNhan;
    private long tongChuyenKhoa;
    private long tongLichHen;
    private long lichHenChoDuyet;
    private long lichHenDaDuyet;
    private long lichHenHoanThanh;
    private long lichHenDaHuy;
}

