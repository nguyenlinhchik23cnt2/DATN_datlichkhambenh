package quanly.phongkham.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DangNhapRequest {

    @NotBlank(message = "Vui lòng nhập tên đăng nhập!")
    private String tenDangNhap;

    @NotBlank(message = "Vui lòng nhập mật khẩu!")
    private String matKhau;
}

