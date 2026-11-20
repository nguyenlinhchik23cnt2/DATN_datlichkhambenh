package quanly.phongkham.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import quanly.phongkham.dto.DangKyBenhNhanRequest;
import quanly.phongkham.dto.DangNhapResponse;
import quanly.phongkham.entity.BenhNhan;
import quanly.phongkham.entity.TaiKhoan;
import quanly.phongkham.repository.BenhNhanRepository;
import quanly.phongkham.repository.TaiKhoanRepository;

import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TaiKhoanServiceTest {

    private final Map<Integer, TaiKhoan> db = new HashMap<>();
    private final Map<Integer, BenhNhan> dbBenhNhan = new HashMap<>();
    private int idCounter = 1;
    private int idBnCounter = 1;

    private PasswordEncoder passwordEncoder;
    private TaiKhoanService service;

    @BeforeEach
    void setUp() {
        db.clear();
        dbBenhNhan.clear();
        idCounter = 1;
        idBnCounter = 1;
        passwordEncoder = new BCryptPasswordEncoder();

        TaiKhoanRepository stubRepository = (TaiKhoanRepository) Proxy.newProxyInstance(
                TaiKhoanRepository.class.getClassLoader(),
                new Class<?>[]{TaiKhoanRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("save".equals(name)) {
                        TaiKhoan tk = (TaiKhoan) args[0];
                        if (tk.getMaTaiKhoan() == null) {
                            tk.setMaTaiKhoan(idCounter++);
                        }
                        db.put(tk.getMaTaiKhoan(), tk);
                        return tk;
                    }
                    if ("findById".equals(name)) {
                        return Optional.ofNullable(db.get((Integer) args[0]));
                    }
                    if ("findByTenDangNhap".equals(name)) {
                        return db.values().stream()
                                .filter(tk -> args[0].equals(tk.getTenDangNhap()))
                                .findFirst();
                    }
                    if ("existsByTenDangNhap".equals(name)) {
                        return db.values().stream()
                                .anyMatch(tk -> args[0].equals(tk.getTenDangNhap()));
                    }
                    if ("existsByEmail".equals(name)) {
                        return db.values().stream()
                                .anyMatch(tk -> args[0].equals(tk.getEmail()));
                    }
                    return null;
                }
        );

        BenhNhanRepository stubBnRepo = (BenhNhanRepository) Proxy.newProxyInstance(
                BenhNhanRepository.class.getClassLoader(),
                new Class<?>[]{BenhNhanRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("save".equals(name)) {
                        BenhNhan bn = (BenhNhan) args[0];
                        if (bn.getMaBenhNhan() == null) {
                            bn.setMaBenhNhan(idBnCounter++);
                        }
                        dbBenhNhan.put(bn.getMaBenhNhan(), bn);
                        return bn;
                    }
                    if ("findByMaTaiKhoan".equals(name)) {
                        Integer tkId = (Integer) args[0];
                        return dbBenhNhan.values().stream()
                                .filter(b -> tkId.equals(b.getMaTaiKhoan()))
                                .findFirst();
                    }
                    return null;
                }
        );

        service = new TaiKhoanService(stubRepository, passwordEncoder, stubBnRepo);
    }

    @Test
    @DisplayName("Lưu tài khoản mới thì mật khẩu thô phải được mã hóa BCrypt")
    void testSaveEncodesRawPassword() {
        TaiKhoan tk = new TaiKhoan();
        tk.setTenDangNhap("bacsi_lan");
        tk.setMatKhau("MatKhau123@");
        tk.setHoTen("Lê Văn Lan");
        tk.setVaiTro("BacSi");

        TaiKhoan saved = service.save(tk);

        assertNotNull(saved.getNgayTao());
        assertTrue(service.isBCryptHash(saved.getMatKhau()));
        assertTrue(passwordEncoder.matches("MatKhau123@", saved.getMatKhau()));
    }

    @Test
    @DisplayName("Nếu mật khẩu đã là mã BCrypt thì không bị mã hóa 2 lần")
    void testSaveDoesNotDoubleEncodeBCryptHash() {
        String existingHash = passwordEncoder.encode("MatKhau123@");
        TaiKhoan tk = new TaiKhoan();
        tk.setTenDangNhap("bacsi_lan");
        tk.setMatKhau(existingHash);
        tk.setNgayTao(LocalDateTime.now());

        TaiKhoan saved = service.save(tk);

        assertEquals(existingHash, saved.getMatKhau());
    }

    @Test
    @DisplayName("Đăng nhập thành công với mật khẩu đúng (đã mã hóa BCrypt)")
    void testAuthenticateSuccess() {
        String rawPassword = "Password@2026";
        String encoded = passwordEncoder.encode(rawPassword);

        TaiKhoan tk = new TaiKhoan();
        tk.setTenDangNhap("admin");
        tk.setMatKhau(encoded);
        tk.setTrangThai(true);
        service.save(tk);

        Optional<TaiKhoan> authUser = service.authenticate("admin", rawPassword);

        assertTrue(authUser.isPresent());
        assertEquals("admin", authUser.get().getTenDangNhap());
    }

    @Test
    @DisplayName("Đăng nhập thất bại khi sai mật khẩu")
    void testAuthenticateFailWrongPassword() {
        String encoded = passwordEncoder.encode("Password@2026");

        TaiKhoan tk = new TaiKhoan();
        tk.setTenDangNhap("admin");
        tk.setMatKhau(encoded);
        tk.setTrangThai(true);
        service.save(tk);

        Optional<TaiKhoan> authUser = service.authenticate("admin", "WrongPassword");

        assertTrue(authUser.isEmpty());
    }

    @Test
    @DisplayName("Đăng nhập thất bại khi tài khoản bị khóa (trangThai = false)")
    void testAuthenticateFailAccountDisabled() {
        String rawPassword = "Password@2026";
        String encoded = passwordEncoder.encode(rawPassword);

        TaiKhoan tk = new TaiKhoan();
        tk.setTenDangNhap("admin");
        tk.setMatKhau(encoded);
        tk.setTrangThai(false);
        service.save(tk);

        Optional<TaiKhoan> authUser = service.authenticate("admin", rawPassword);

        assertTrue(authUser.isEmpty());
    }

    @Test
    @DisplayName("Tương thích ngược: Mật khẩu cũ dạng plain-text đăng nhập được và tự động nâng cấp sang BCrypt")
    void testAuthenticateLegacyPlainTextAutoUpgrades() {
        String rawPassword = "plainPassword123";

        TaiKhoan tk = new TaiKhoan();
        tk.setTenDangNhap("old_user");
        tk.setMatKhau(rawPassword); // mat khau cu chua ma hoa
        tk.setTrangThai(true);
        tk.setNgayTao(LocalDateTime.now());
        db.put(1, tk);
        tk.setMaTaiKhoan(1);

        Optional<TaiKhoan> authUser = service.authenticate("old_user", rawPassword);

        assertTrue(authUser.isPresent());
        // Kiem tra mat khau da duoc nang cap len BCrypt
        assertTrue(service.isBCryptHash(tk.getMatKhau()));
        assertTrue(passwordEncoder.matches(rawPassword, tk.getMatKhau()));
    }

    @Test
    @DisplayName("Đổi mật khẩu thành công khi nhập đúng mật khẩu cũ")
    void testChangePasswordSuccess() {
        String oldPass = "OldPassword@123";
        String newPass = "NewPassword@456";
        String oldHash = passwordEncoder.encode(oldPass);

        TaiKhoan tk = new TaiKhoan();
        tk.setMaTaiKhoan(1);
        tk.setTenDangNhap("user1");
        tk.setMatKhau(oldHash);
        db.put(1, tk);

        boolean result = service.changePassword(1, oldPass, newPass);

        assertTrue(result);
        assertTrue(passwordEncoder.matches(newPass, tk.getMatKhau()));
    }

    @Test
    @DisplayName("Đổi mật khẩu thất bại khi nhập sai mật khẩu cũ")
    void testChangePasswordFailsWrongOldPassword() {
        String oldPass = "OldPassword@123";
        String oldHash = passwordEncoder.encode(oldPass);

        TaiKhoan tk = new TaiKhoan();
        tk.setMaTaiKhoan(1);
        tk.setTenDangNhap("user1");
        tk.setMatKhau(oldHash);
        db.put(1, tk);

        boolean result = service.changePassword(1, "IncorrectOldPass", "NewPassword@456");

        assertFalse(result);
    }

    @Test
    @DisplayName("Bệnh nhân đăng ký tài khoản thành công: tạo TaiKhoan mã hóa BCrypt và tạo hồ sơ BenhNhan")
    void testDangKyBenhNhanSuccess() {
        DangKyBenhNhanRequest req = DangKyBenhNhanRequest.builder()
                .tenDangNhap("benhnhan_minh")
                .matKhau("MatKhau123@")
                .hoTen("Trần Văn Minh")
                .email("minh@gmail.com")
                .soDienThoai("0909123456")
                .ngaySinh(LocalDate.of(1995, 5, 20))
                .gioiTinh("Nam")
                .diaChi("Hà Nội")
                .build();

        DangNhapResponse res = service.dangKyBenhNhan(req);

        assertNotNull(res);
        assertNotNull(res.getMaTaiKhoan());
        assertNotNull(res.getMaBenhNhan());
        assertEquals("benhnhan_minh", res.getTenDangNhap());
        assertEquals("BenhNhan", res.getVaiTro());
        assertEquals("Trần Văn Minh", res.getHoTen());
        assertEquals(LocalDate.of(1995, 5, 20), res.getNgaySinh());

        // Kiem tra password da duoc hash BCrypt
        TaiKhoan saved = db.get(res.getMaTaiKhoan());
        assertTrue(service.isBCryptHash(saved.getMatKhau()));
        assertTrue(passwordEncoder.matches("MatKhau123@", saved.getMatKhau()));

        // Kiem tra ho so benh nhan duoc tao va map dung
        BenhNhan savedBn = dbBenhNhan.get(res.getMaBenhNhan());
        assertEquals(saved.getMaTaiKhoan(), savedBn.getMaTaiKhoan());
        assertEquals("Hà Nội", savedBn.getDiaChi());
    }

    @Test
    @DisplayName("Bệnh nhân đăng ký thất bại khi trùng tên đăng nhập")
    void testDangKyBenhNhanDuplicateUsernameFails() {
        TaiKhoan tk = new TaiKhoan();
        tk.setTenDangNhap("benhnhan_minh");
        tk.setMatKhau("123");
        service.save(tk);

        DangKyBenhNhanRequest req = DangKyBenhNhanRequest.builder()
                .tenDangNhap("benhnhan_minh")
                .matKhau("MatKhau123@")
                .hoTen("Trần Văn Minh")
                .soDienThoai("0909123456")
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.dangKyBenhNhan(req));
        assertEquals("Tên đăng nhập đã tồn tại!", ex.getMessage());
    }
}
