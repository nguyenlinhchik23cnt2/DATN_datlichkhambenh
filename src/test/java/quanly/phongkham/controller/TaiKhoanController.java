package quanly.phongkham.controller;

import quanly.phongkham.dto.DangKyBenhNhanRequest;
import quanly.phongkham.dto.DangNhapRequest;
import quanly.phongkham.dto.DangNhapResponse;
import quanly.phongkham.dto.DoiMatKhauRequest;
import quanly.phongkham.entity.TaiKhoan;
import quanly.phongkham.service.TaiKhoanService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tai-khoan")
@CrossOrigin(origins = "http://localhost:5173")
public class TaiKhoanController {

    private final TaiKhoanService service;

    @Autowired
    private SecurityContextRepository securityContextRepository;

    public TaiKhoanController(TaiKhoanService service) {
        this.service = service;
    }

    @GetMapping
    public List<TaiKhoan> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaiKhoan> getById(@PathVariable Integer id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ten-dang-nhap/{tenDangNhap}")
    public ResponseEntity<TaiKhoan> getByTenDangNhap(
            @PathVariable String tenDangNhap) {

        return service.getByTenDangNhap(tenDangNhap)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/dang-nhap")
    public ResponseEntity<?> dangNhap(
            @RequestBody DangNhapRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        if (request.getTenDangNhap() == null || request.getMatKhau() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng nhập tên đăng nhập và mật khẩu!"));
        }

        Optional<TaiKhoan> userOpt = service.authenticate(request.getTenDangNhap(), request.getMatKhau());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Tên đăng nhập hoặc mật khẩu không chính xác!"));
        }

        TaiKhoan user = userOpt.get();
        luuPhienDangNhap(user, httpRequest, httpResponse);
        return ResponseEntity.ok(service.toDangNhapResponse(user, "Đăng nhập thành công!"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody DangNhapRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        return dangNhap(request, httpRequest, httpResponse);
    }

    @PostMapping("/dang-ky")
    public ResponseEntity<?> dangKy(
            @Valid @RequestBody DangKyBenhNhanRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        try {
            DangNhapResponse response = service.dangKyBenhNhan(request);
            service.getById(response.getMaTaiKhoan())
                    .ifPresent(tk -> luuPhienDangNhap(tk, httpRequest, httpResponse));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/dang-ky-benh-nhan")
    public ResponseEntity<?> dangKyBenhNhan(
            @Valid @RequestBody DangKyBenhNhanRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        return dangKy(request, httpRequest, httpResponse);
    }

    /**
     * Ghi nhan phien dang nhap vao SecurityContext de cac API can quyen
     * (duyet/huy lich hen, thong ke, ...) nhan dien duoc nguoi dung.
     */
    private void luuPhienDangNhap(TaiKhoan user, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        if (securityContextRepository == null) {
            return;
        }

        String vaiTro = user.getVaiTro() == null ? "" : user.getVaiTro().trim();
        String quyen = switch (vaiTro.toLowerCase(Locale.ROOT)) {
            case "admin" -> "Admin";
            case "bacsi" -> "BacSi";
            case "tieptan" -> "TiepTan";
            case "benhnhan" -> "BenhNhan";
            default -> vaiTro;
        };

        var authentication = new UsernamePasswordAuthenticationToken(
                user.getTenDangNhap(),
                null,
                List.of(new SimpleGrantedAuthority(quyen))
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody TaiKhoan taiKhoan) {
        if (service.existsByTenDangNhap(taiKhoan.getTenDangNhap())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Tên đăng nhập đã tồn tại!"));
        }

        if (taiKhoan.getEmail() != null && !taiKhoan.getEmail().isBlank()
                && service.existsByEmail(taiKhoan.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email đã tồn tại!"));
        }

        // Chong leo thang dac quyen: nguoi dang ky cong khai chi duoc tao tai khoan BenhNhan.
        // Chi Admin dang nhap moi duoc chi dinh vai tro khac.
        if (!laAdmin()) {
            taiKhoan.setVaiTro("BenhNhan");
        }

        TaiKhoan saved = service.save(taiKhoan);
        return ResponseEntity.ok(saved);
    }

    /**
     * Kiem tra nguoi goi hien tai co phai Admin hay khong.
     */
    private boolean laAdmin() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "Admin".equals(a.getAuthority()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @RequestBody TaiKhoan taiKhoan) {

        var existingOpt = service.getById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        TaiKhoan existing = existingOpt.get();
        taiKhoan.setMaTaiKhoan(id);

        // Giu nguyen mat khau neu request khong truyen mat khau moi
        if (taiKhoan.getMatKhau() == null || taiKhoan.getMatKhau().isBlank()) {
            taiKhoan.setMatKhau(existing.getMatKhau());
        }

        // Giu nguyen ngay tao neu khong truyen
        if (taiKhoan.getNgayTao() == null) {
            taiKhoan.setNgayTao(existing.getNgayTao());
        }

        return ResponseEntity.ok(service.save(taiKhoan));
    }

    @PutMapping("/{id}/doi-mat-khau")
    public ResponseEntity<?> doiMatKhau(
            @PathVariable Integer id,
            @RequestBody DoiMatKhauRequest request) {

        if (request.getMatKhauCu() == null || request.getMatKhauMoi() == null || request.getMatKhauMoi().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng cung cấp mật khẩu cũ và mật khẩu mới!"));
        }

        boolean thanhCong = service.changePassword(id, request.getMatKhauCu(), request.getMatKhauMoi());
        if (!thanhCong) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mật khẩu cũ không chính xác hoặc tài khoản không tồn tại!"));
        }

        return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công!"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (service.getById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private DangNhapResponse toResponse(TaiKhoan tk, String message) {
        return service.toDangNhapResponse(tk, message);
    }
}