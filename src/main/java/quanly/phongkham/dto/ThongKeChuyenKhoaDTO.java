package quanly.phongkham.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThongKeChuyenKhoaDTO {
    private Integer maChuyenKhoa;
    private String tenChuyenKhoa;
    private long soLuotKham;
}

