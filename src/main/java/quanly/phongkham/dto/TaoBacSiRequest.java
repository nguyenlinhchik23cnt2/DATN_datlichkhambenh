package quanly.phongkham.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaoBacSiRequest {

    @NotBlank(message = "Vui lòng nhập tên đăng nhập!")
    @Size(max = 50, message = "Tên đăng nhập tối đa 50 ký tự!")
    private String tenDangNhap;

    @NotBlank(message = "Vui lòng nhập mật khẩu!")
    @Size(min = 6, max = 100, message = "Mật khẩu phải từ 6 đến 100 ký tự!")
    private String matKhau;

    @NotBlank(message = "Vui lòng nhập họ tên!")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự!")
    private String hoTen;

    @Email(message = "Email không hợp lệ!")
    @Size(max = 100, message = "Email tối đa 100 ký tự!")
    private String email;

    @Size(max = 15, message = "Số điện thoại tối đa 15 ký tự!")
    private String soDienThoai;

    @NotNull(message = "Vui lòng chọn chuyên khoa!")
    private Integer maChuyenKhoa;

    @Size(max = 50, message = "Học vị tối đa 50 ký tự!")
    private String hocVi;

    @Size(max = 500, message = "Kinh nghiệm tối đa 500 ký tự!")
    private String kinhNghiem;

    @Size(max = 1000, message = "Giới thiệu tối đa 1000 ký tự!")
    private String gioiThieu;
}

