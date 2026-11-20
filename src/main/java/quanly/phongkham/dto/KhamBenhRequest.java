package quanly.phongkham.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhamBenhRequest {

    @NotNull(message = "Vui lòng chọn lịch hẹn!")
    private Integer maLichHen;

    @Size(max = 1000, message = "Triệu chứng tối đa 1000 ký tự!")
    private String trieuChung;

    @NotBlank(message = "Vui lòng nhập chẩn đoán!")
    @Size(max = 1000, message = "Chẩn đoán tối đa 1000 ký tự!")
    private String chanDoan;

    @Size(max = 1000, message = "Lời dặn tối đa 1000 ký tự!")
    private String loiDanBacSi;

    @Size(max = 500, message = "Ghi chú đơn thuốc tối đa 500 ký tự!")
    private String ghiChuDonThuoc;

    @Valid
    private List<@Valid ChiTietThuocItem> danhSachThuoc;
}

