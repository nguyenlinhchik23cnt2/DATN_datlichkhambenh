package quanly.phongkham.dto;

import lombok.*;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhungGioSlotDTO {
    private Integer maLich;
    private LocalTime gioBatDau;
    private LocalTime gioKetThuc;
    private Boolean conTrong;
}

