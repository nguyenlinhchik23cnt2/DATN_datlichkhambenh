
package quanly.phongkham.service;

import quanly.phongkham.dto.DangKyBenhNhanRequest;
import quanly.phongkham.dto.DangNhapResponse;
import quanly.phongkham.entity.BenhNhan;
import quanly.phongkham.entity.TaiKhoan;
import quanly.phongkham.repository.BenhNhanRepository;
import quanly.phongkham.repository.TaiKhoanRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TaiKhoanService {

    private final TaiKhoanRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final BenhNhanRepository benhNhanRepository;

    public TaiKhoanService(
            TaiKhoanRepository repository,
            PasswordEncoder passwordEncoder,
            BenhNhanRepository benhNhanRepository) {

        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.benhNhanRepository = benhNhanRepository;
    }

    // ================= LẤY DANH SÁCH TÀI KHOẢN =================

    public List<TaiKhoan> getAll() {
        return repository.findAll();
    }

    public Optional<TaiKhoan> getById(Integer id) {
        return repository.findById(id);
    }

    public Optional<TaiKhoan> getByTenDangNhap(String tenDangNhap) {
        return repository.findByTenDangNhap(tenDangNhap);
    }

    // ================= LƯU TÀI KHOẢN =================

    @Transactional
    public TaiKhoan save(TaiKhoan taiKhoan) {

        if (taiKhoan.getNgayTao() == null) {
            taiKhoan.setNgayTao(LocalDateTime.now());
        }

        String matKhau = taiKhoan.getMatKhau();

        if (matKhau != null && !matKhau.isBlank()) {
            if (!isBCryptHash(matKhau)) {
                taiKhoan.setMatKhau(passwordEncoder.encode(matKhau));
            }
        }

        return repository.save(taiKhoan);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }

    public boolean existsByTenDangNhap(String tenDangNhap) {
        return repository.existsByTenDangNhap(tenDangNhap);
    }

    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    // ================= ĐĂNG NHẬP =================

    @Transactional
    public Optional<TaiKhoan> authenticate(
            String tenDangNhap,
            String rawPassword) {

        if (tenDangNhap == null || rawPassword == null) {
            return Optional.empty();
        }

        Optional<TaiKhoan> opt =
                repository.findByTenDangNhap(tenDangNhap.trim());

        if (opt.isEmpty()) {
            return Optional.empty();
        }

        TaiKhoan user = opt.get();

        // Không cho phép tài khoản bị khóa đăng nhập.
        if (Boolean.FALSE.equals(user.getTrangThai())) {
            return Optional.empty();
        }

        String currentHash = user.getMatKhau();

        if (currentHash == null || currentHash.isBlank()) {
            return Optional.empty();
        }

        // Kiểm tra mật khẩu BCrypt.
        if (isBCryptHash(currentHash)) {
            if (passwordEncoder.matches(rawPassword, currentHash)) {
                return Optional.of(user);
            }

            return Optional.empty();
        }

        // Tương thích với tài khoản cũ đang lưu mật khẩu dạng văn bản.
        // Khi đăng nhập đúng, tự động chuyển sang BCrypt.
        if (currentHash.equals(rawPassword)) {
            user.setMatKhau(passwordEncoder.encode(rawPassword));
            repository.save(user);

            return Optional.of(user);
        }

        return Optional.empty();
    }

    // ================= ĐỔI MẬT KHẨU =================

    @Transactional
    public boolean changePassword(
            Integer id,
            String matKhauCu,
            String matKhauMoi) {

        if (id == null
                || matKhauCu == null
                || matKhauMoi == null
                || matKhauMoi.isBlank()) {
            return false;
        }

        Optional<TaiKhoan> opt = repository.findById(id);

        if (opt.isEmpty()) {
            return false;
        }

        TaiKhoan user = opt.get();

        String currentHash = user.getMatKhau();

        if (currentHash == null || currentHash.isBlank()) {
            return false;
        }

        boolean matches;

        if (isBCryptHash(currentHash)) {
            matches = passwordEncoder.matches(
                    matKhauCu,
                    currentHash
            );
        } else {
            // Hỗ trợ tài khoản cũ chưa được chuyển sang BCrypt.
            matches = currentHash.equals(matKhauCu);
        }

        if (!matches) {
            return false;
        }

        user.setMatKhau(passwordEncoder.encode(matKhauMoi));

        repository.save(user);

        return true;
    }

    // ================= ĐĂNG KÝ BỆNH NHÂN =================

    @Transactional
    public DangNhapResponse dangKyBenhNhan(
            DangKyBenhNhanRequest req) {

        if (req.getTenDangNhap() == null
                || req.getTenDangNhap().isBlank()) {
            throw new IllegalArgumentException(
                    "Tên đăng nhập không được để trống!"
            );
        }

        if (req.getMatKhau() == null
                || req.getMatKhau().isBlank()) {
            throw new IllegalArgumentException(
                    "Mật khẩu không được để trống!"
            );
        }

        if (req.getHoTen() == null
                || req.getHoTen().isBlank()) {
            throw new IllegalArgumentException(
                    "Họ và tên không được để trống!"
            );
        }

        if (req.getSoDienThoai() == null
                || req.getSoDienThoai().isBlank()) {
            throw new IllegalArgumentException(
                    "Số điện thoại không được để trống!"
            );
        }

        String tenDangNhap = req.getTenDangNhap().trim();

        if (existsByTenDangNhap(tenDangNhap)) {
            throw new IllegalArgumentException(
                    "Tên đăng nhập đã tồn tại!"
            );
        }

        String email = req.getEmail();

        if (email != null && !email.isBlank()
                && existsByEmail(email.trim())) {
            throw new IllegalArgumentException(
                    "Email đã tồn tại!"
            );
        }

        // Tạo tài khoản bệnh nhân.
        TaiKhoan tk = new TaiKhoan();

        tk.setTenDangNhap(tenDangNhap);
        tk.setMatKhau(req.getMatKhau());
        tk.setHoTen(req.getHoTen().trim());

        tk.setEmail(
                email != null && !email.isBlank()
                        ? email.trim()
                        : null
        );

        tk.setSoDienThoai(req.getSoDienThoai().trim());
        tk.setVaiTro("BenhNhan");
        tk.setTrangThai(true);
        tk.setNgayTao(LocalDateTime.now());

        // save() tự động mã hóa mật khẩu BCrypt.
        TaiKhoan savedTk = save(tk);

        // Tạo hồ sơ bệnh nhân liên kết với tài khoản.
        BenhNhan bn = new BenhNhan();

        bn.setMaTaiKhoan(savedTk.getMaTaiKhoan());
        bn.setNgaySinh(req.getNgaySinh());
        bn.setGioiTinh(req.getGioiTinh());
        bn.setDiaChi(req.getDiaChi());

        BenhNhan savedBn = benhNhanRepository.save(bn);

        return DangNhapResponse.builder()
                .maTaiKhoan(savedTk.getMaTaiKhoan())
                .maBenhNhan(savedBn.getMaBenhNhan())
                .tenDangNhap(savedTk.getTenDangNhap())
                .hoTen(savedTk.getHoTen())
                .email(savedTk.getEmail())
                .soDienThoai(savedTk.getSoDienThoai())
                .vaiTro(savedTk.getVaiTro())
                .trangThai(savedTk.getTrangThai())
                .ngaySinh(req.getNgaySinh())
                .gioiTinh(req.getGioiTinh())
                .diaChi(req.getDiaChi())
                .ngayTao(savedTk.getNgayTao())
                .message("Đăng ký tài khoản bệnh nhân thành công!")
                .build();
    }

    // ================= CHUYỂN ĐỔI RESPONSE =================

    public DangNhapResponse toDangNhapResponse(
            TaiKhoan tk,
            String message) {

        var builder = DangNhapResponse.builder()
                .maTaiKhoan(tk.getMaTaiKhoan())
                .tenDangNhap(tk.getTenDangNhap())
                .hoTen(tk.getHoTen())
                .email(tk.getEmail())
                .soDienThoai(tk.getSoDienThoai())
                .vaiTro(tk.getVaiTro())
                .trangThai(tk.getTrangThai())
                .ngayTao(tk.getNgayTao())
                .message(message);

        if (tk.getMaTaiKhoan() != null
                && tk.getVaiTro() != null
                && "BenhNhan".equalsIgnoreCase(tk.getVaiTro().trim())) {

            Optional<BenhNhan> bnOpt =
                    benhNhanRepository.findByMaTaiKhoan(
                            tk.getMaTaiKhoan()
                    );

            if (bnOpt.isPresent()) {
                BenhNhan bn = bnOpt.get();

                builder.maBenhNhan(bn.getMaBenhNhan())
                        .ngaySinh(bn.getNgaySinh())
                        .gioiTinh(bn.getGioiTinh())
                        .diaChi(bn.getDiaChi());
            }
        }

        return builder.build();
    }

    // ================= KIỂM TRA MẬT KHẨU BCRYPT =================

    public boolean isBCryptHash(String password) {

        if (password == null || password.length() != 60) {
            return false;
        }

        return password.startsWith("$2a$")
                || password.startsWith("$2b$")
                || password.startsWith("$2y$");
    }
}
