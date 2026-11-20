package quanly.phongkham.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DangNhapResponse {
    private Integer maTaiKhoan;
    private Integer maBenhNhan;
    private String tenDangNhap;
    private String hoTen;
    private String email;
    private String soDienThoai;
    private String vaiTro;
    private Boolean trangThai;
    private LocalDate ngaySinh;
    private String gioiTinh;
    private String diaChi;
    private LocalDateTime ngayTao;
    private String message;
}
