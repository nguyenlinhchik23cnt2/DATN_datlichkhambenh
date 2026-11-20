package quanly.phongkham.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietThuocItem {

    @NotBlank(message = "Tên thuốc không được để trống!")
    @Size(max = 150, message = "Tên thuốc tối đa 150 ký tự!")
    private String tenThuoc;

    @Positive(message = "Số lượng thuốc phải lớn hơn 0!")
    private Integer soLuong;

    @Size(max = 255, message = "Liều dùng tối đa 255 ký tự!")
    private String lieuDung;

    @Size(max = 500, message = "Cách dùng tối đa 500 ký tự!")
    private String cachDung;
}

