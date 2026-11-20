package quanly.phongkham.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DangKyBenhNhanRequest {

    @NotBlank(message = "Tên đăng nhập không được để trống!")
    @Size(min = 3, max = 50, message = "Tên đăng nhập từ 3 đến 50 ký tự!")
    private String tenDangNhap;

    @NotBlank(message = "Mật khẩu không được để trống!")
    @Size(min = 6, max = 100, message = "Mật khẩu tối thiểu 6 ký tự!")
    private String matKhau;

    @NotBlank(message = "Họ và tên không được để trống!")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự!")
    private String hoTen;

    private String email;

    @NotBlank(message = "Số điện thoại không được để trống!")
    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự!")
    private String soDienThoai;

    private LocalDate ngaySinh;

    private String gioiTinh;

    private String diaChi;
}

