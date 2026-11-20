package quanly.phongkham.dto;
   // import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatLichHenRequest {

    @NotNull(message = "Vui lòng chọn bệnh nhân!")
    private Integer maBenhNhan;

    @NotNull(message = "Vui lòng chọn ca làm việc!")
    private Integer maLich;

    @NotNull(message = "Vui lòng chọn giờ khám!")
    private LocalTime gioBatDau;

    @NotBlank(message = "Vui lòng nhập lý do khám!")
    @Size(max = 500, message = "Lý do khám tối đa 500 ký tự!")
    private String lyDoKham;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự!")
    private String ghiChu;

    @Size(max = 100, message = "Tên bệnh nhân tối đa 100 ký tự!")
    private String tenBenhNhan;

    
}

