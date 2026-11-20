
package quanly.phongkham.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import quanly.phongkham.entity.BacSi;
import quanly.phongkham.entity.BenhNhan;
import quanly.phongkham.entity.TaiKhoan;

import quanly.phongkham.repository.BacSiRepository;
import quanly.phongkham.repository.BenhNhanRepository;
import quanly.phongkham.repository.TaiKhoanRepository;

import java.util.Locale;
import java.util.Optional;

@Component
public class NguoiDungHienTai {

    private final TaiKhoanRepository taiKhoanRepository;
    private final BenhNhanRepository benhNhanRepository;
    private final BacSiRepository bacSiRepository;

    public NguoiDungHienTai(
            TaiKhoanRepository taiKhoanRepository,
            BenhNhanRepository benhNhanRepository,
            BacSiRepository bacSiRepository) {

        this.taiKhoanRepository = taiKhoanRepository;
        this.benhNhanRepository = benhNhanRepository;
        this.bacSiRepository = bacSiRepository;
    }

    // Lấy tài khoản đang đăng nhập
    public Optional<TaiKhoan> taiKhoan() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof String tenDangNhap)
                || tenDangNhap.isBlank()
                || "anonymousUser".equals(tenDangNhap)) {
            return Optional.empty();
        }

        return taiKhoanRepository.findByTenDangNhap(tenDangNhap);
    }

    // Kiểm tra quyền
    public boolean coQuyen(String quyen) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(a -> quyen.equals(a.getAuthority()));
    }

    public boolean laAdmin() {
        return coQuyen("Admin");
    }

    public boolean laBacSi() {
        return coQuyen("BacSi");
    }

    public boolean laTiepTan() {
        return coQuyen("TiepTan");
    }

    public boolean laBenhNhan() {
        return coQuyen("BenhNhan");
    }

    // Lấy mã bác sĩ từ tài khoản đang đăng nhập
    public Optional<Integer> maBacSi() {
        if (!laBacSi()) {
            return Optional.empty();
        }

        return taiKhoan()
                .flatMap(tk ->
                        bacSiRepository.findByMaTaiKhoan(tk.getMaTaiKhoan()))
                .map(BacSi::getMaBacSi);
    }

    // Lấy mã bệnh nhân từ tài khoản đang đăng nhập
    public Optional<Integer> maBenhNhan() {
        if (!laBenhNhan()) {
            return Optional.empty();
        }

        return taiKhoan()
                .flatMap(tk ->
                        benhNhanRepository.findByMaTaiKhoan(tk.getMaTaiKhoan()))
                .map(BenhNhan::getMaBenhNhan);
    }

    // Admin, bác sĩ, tiếp tân được xem dữ liệu bệnh nhân
    public boolean laNhanVien() {
        return laAdmin() || laBacSi() || laTiepTan();
    }

    // Kiểm tra bệnh nhân có phải chủ sở hữu dữ liệu không
    public boolean laChuSoHuuBenhNhan(Integer maBenhNhan) {
        if (maBenhNhan == null) {
            return false;
        }

        return maBenhNhan()
                .map(maBenhNhan::equals)
                .orElse(false);
    }
}
