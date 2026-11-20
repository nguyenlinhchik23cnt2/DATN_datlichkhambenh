package quanly.phongkham.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BacSiChiTietDTO {
    private Integer maBacSi;
    private Integer maTaiKhoan;
    private String tenBacSi;
    private String hocVi;
    private String kinhNghiem;
    private String gioiThieu;
    private Boolean trangThai;

    private Integer maChuyenKhoa;
    private String tenChuyenKhoa;

    private String email;
    private String soDienThoai;
}

