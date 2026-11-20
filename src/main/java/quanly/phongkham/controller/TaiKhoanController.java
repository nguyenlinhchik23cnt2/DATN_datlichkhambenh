
package quanly.phongkham.controller;

import quanly.phongkham.dto.DangKyBenhNhanRequest;
import quanly.phongkham.dto.DangNhapRequest;
import quanly.phongkham.dto.DangNhapResponse;
import quanly.phongkham.dto.DoiMatKhauRequest;
import quanly.phongkham.entity.TaiKhoan;
import quanly.phongkham.service.TaiKhoanService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
    private final SecurityContextRepository securityContextRepository;

    public TaiKhoanController(
            TaiKhoanService service,
            SecurityContextRepository securityContextRepository) {
        this.service = service;
        this.securityContextRepository = securityContextRepository;
    }

    // ================= KIỂM TRA QUYỀN =================

    private Authentication authentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private boolean daDangNhap() {
        Authentication auth = authentication();

        return auth != null
                && auth.isAuthenticated()
                && auth.getPrincipal() instanceof String;
    }

    private boolean laAdmin() {
        Authentication auth = authentication();

        return auth != null
                && auth.isAuthenticated()
                && auth.getAuthorities().stream()
                .anyMatch(a -> "Admin".equals(a.getAuthority()));
    }

    private boolean laChuTaiKhoan(Integer maTaiKhoan) {
        if (!daDangNhap() || maTaiKhoan == null) {
            return false;
        }

        String tenDangNhap = authentication().getName();

        return service.getById(maTaiKhoan)
                .map(tk -> tenDangNhap.equals(tk.getTenDangNhap()))
                .orElse(false);
    }

    private boolean duocTruyCapTaiKhoan(Integer maTaiKhoan) {
        return laAdmin() || laChuTaiKhoan(maTaiKhoan);
    }

    private ResponseEntity<?> tuChoi(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", message));
    }

    // ================= LẤY THÔNG TIN TÀI KHOẢN =================

    @GetMapping
    public ResponseEntity<?> getAll() {
        if (!laAdmin()) {
            return tuChoi("Chỉ Admin mới được xem danh sách tài khoản!");
        }

        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {

        if (!duocTruyCapTaiKhoan(id)) {
            return tuChoi("Bạn không có quyền xem tài khoản này!");
        }

        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ten-dang-nhap/{tenDangNhap}")
    public ResponseEntity<?> getByTenDangNhap(
            @PathVariable String tenDangNhap) {

        if (!laAdmin()
                && (!daDangNhap()
                || !authentication().getName().equals(tenDangNhap))) {
            return tuChoi("Bạn không có quyền xem tài khoản này!");
        }

        return service.getByTenDangNhap(tenDangNhap)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ================= ĐĂNG NHẬP =================

    @PostMapping("/dang-nhap")
    public ResponseEntity<?> dangNhap(
            @RequestBody DangNhapRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        if (request.getTenDangNhap() == null
                || request.getTenDangNhap().isBlank()
                || request.getMatKhau() == null
                || request.getMatKhau().isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Vui lòng nhập tên đăng nhập và mật khẩu!"
                    ));
        }

        Optional<TaiKhoan> userOpt = service.authenticate(
                request.getTenDangNhap().trim(),
                request.getMatKhau()
        );

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "Tên đăng nhập hoặc mật khẩu không chính xác!"
                    ));
        }

        TaiKhoan user = userOpt.get();

        if (Boolean.FALSE.equals(user.getTrangThai())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message",
                            "Tài khoản đã bị khóa!"
                    ));
        }

        if (!luuPhienDangNhap(user, httpRequest, httpResponse)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message",
                            "Vai trò tài khoản không hợp lệ!"
                    ));
        }

        return ResponseEntity.ok(
                service.toDangNhapResponse(
                        user,
                        "Đăng nhập thành công!"
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody DangNhapRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        return dangNhap(request, httpRequest, httpResponse);
    }

    // ================= ĐĂNG XUẤT =================

    @PostMapping("/dang-xuat")
    public ResponseEntity<?> dangXuat(
            HttpServletRequest request) {

        SecurityContextHolder.clearContext();

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.ok(
                Map.of("message", "Đăng xuất thành công!")
        );
    }

    // ================= ĐĂNG KÝ BỆNH NHÂN =================

    @PostMapping("/dang-ky")
    public ResponseEntity<?> dangKy(
            @Valid @RequestBody DangKyBenhNhanRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        try {
            DangNhapResponse response =
                    service.dangKyBenhNhan(request);

            service.getById(response.getMaTaiKhoan())
                    .ifPresent(tk ->
                            luuPhienDangNhap(
                                    tk,
                                    httpRequest,
                                    httpResponse
                            )
                    );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/dang-ky-benh-nhan")
    public ResponseEntity<?> dangKyBenhNhan(
            @Valid @RequestBody DangKyBenhNhanRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        return dangKy(request, httpRequest, httpResponse);
    }

    // ================= LƯU PHIÊN ĐĂNG NHẬP =================

    private boolean luuPhienDangNhap(
            TaiKhoan user,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        if (user == null || user.getTenDangNhap() == null) {
            return false;
        }

        if (Boolean.FALSE.equals(user.getTrangThai())) {
            return false;
        }

        String vaiTro = user.getVaiTro() == null
                ? ""
                : user.getVaiTro().trim();

        String quyen = switch (vaiTro.toLowerCase(Locale.ROOT)) {
            case "admin" -> "Admin";
            case "bacsi" -> "BacSi";
            case "tieptan" -> "TiepTan";
            case "benhnhan" -> "BenhNhan";
            default -> "";
        };

        if (quyen.isBlank()) {
            return false;
        }

        var authorities = List.of(
                new SimpleGrantedAuthority(quyen)
        );

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        user.getTenDangNhap(),
                        null,
                        authorities
                );

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(
                context,
                httpRequest,
                httpResponse
        );

        return true;
    }

    // ================= TẠO TÀI KHOẢN =================

    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody TaiKhoan taiKhoan) {

        if (taiKhoan.getTenDangNhap() == null
                || taiKhoan.getTenDangNhap().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Vui lòng nhập tên đăng nhập!"));
        }

        if (service.existsByTenDangNhap(taiKhoan.getTenDangNhap())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Tên đăng nhập đã tồn tại!"));
        }

        if (taiKhoan.getEmail() != null
                && !taiKhoan.getEmail().isBlank()
                && service.existsByEmail(taiKhoan.getEmail())) {

            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email đã tồn tại!"));
        }

        // Tài khoản tạo từ API công khai chỉ được mang vai trò BenhNhan.
        if (!laAdmin()) {
            taiKhoan.setVaiTro("BenhNhan");
        }

        TaiKhoan saved = service.save(taiKhoan);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ================= CẬP NHẬT TÀI KHOẢN =================

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @RequestBody TaiKhoan taiKhoan) {

        if (!laAdmin()) {
            return tuChoi("Chỉ Admin mới được cập nhật tài khoản!");
        }

        Optional<TaiKhoan> existingOpt = service.getById(id);

        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        TaiKhoan existing = existingOpt.get();

        taiKhoan.setMaTaiKhoan(id);

        // Không thay đổi mật khẩu nếu không gửi mật khẩu mới.
        if (taiKhoan.getMatKhau() == null
                || taiKhoan.getMatKhau().isBlank()) {
            taiKhoan.setMatKhau(existing.getMatKhau());
        }

        // Giữ ngày tạo ban đầu.
        if (taiKhoan.getNgayTao() == null) {
            taiKhoan.setNgayTao(existing.getNgayTao());
        }

        return ResponseEntity.ok(service.save(taiKhoan));
    }

    // ================= ĐỔI MẬT KHẨU =================

    @PutMapping("/{id}/doi-mat-khau")
    public ResponseEntity<?> doiMatKhau(
            @PathVariable Integer id,
            @RequestBody DoiMatKhauRequest request) {

        if (!duocTruyCapTaiKhoan(id)) {
            return tuChoi("Bạn không có quyền đổi mật khẩu tài khoản này!");
        }

        if (request.getMatKhauCu() == null
                || request.getMatKhauMoi() == null
                || request.getMatKhauMoi().isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Vui lòng cung cấp mật khẩu cũ và mật khẩu mới!"
                    ));
        }

        boolean thanhCong = service.changePassword(
                id,
                request.getMatKhauCu(),
                request.getMatKhauMoi()
        );

        if (!thanhCong) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Mật khẩu cũ không chính xác hoặc tài khoản không tồn tại!"
                    ));
        }

        return ResponseEntity.ok(
                Map.of("message", "Đổi mật khẩu thành công!")
        );
    }

    // ================= XÓA TÀI KHOẢN =================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable Integer id) {

        if (!laAdmin()) {
            return tuChoi("Chỉ Admin mới được xóa tài khoản!");
        }

        if (service.getById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        service.delete(id);

        return ResponseEntity.noContent().build();
    }
    @GetMapping("/me")
public ResponseEntity<?> me() {
    if (!daDangNhap()) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Chưa đăng nhập!"));
    }

    return service.getByTenDangNhap(authentication().getName())
            .<ResponseEntity<?>>map(tk ->
                    ResponseEntity.ok(service.toDangNhapResponse(tk, "OK")))
            .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Tài khoản không tồn tại!")));
}
}
