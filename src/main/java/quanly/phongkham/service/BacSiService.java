package quanly.phongkham.service;

import quanly.phongkham.dto.BacSiChiTietDTO;
import quanly.phongkham.dto.TaoBacSiRequest;
import quanly.phongkham.entity.BacSi;
import quanly.phongkham.entity.ChuyenKhoa;
import quanly.phongkham.entity.TaiKhoan;
import quanly.phongkham.repository.BacSiRepository;
import quanly.phongkham.repository.ChuyenKhoaRepository;
import quanly.phongkham.repository.TaiKhoanRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BacSiService {

    private final BacSiRepository repository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final ChuyenKhoaRepository chuyenKhoaRepository;
    private final PasswordEncoder passwordEncoder;

    public BacSiService(
            BacSiRepository repository,
            TaiKhoanRepository taiKhoanRepository,
            ChuyenKhoaRepository chuyenKhoaRepository,
            PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.taiKhoanRepository = taiKhoanRepository;
        this.chuyenKhoaRepository = chuyenKhoaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<BacSi> getAll() {
        return repository.findAll();
    }

    public List<BacSi> getActive() {
        return repository.findByTrangThaiTrue();
    }

    public Optional<BacSi> getById(Integer id) {
        return repository.findById(id);
    }

    public Optional<BacSi> getByMaTaiKhoan(Integer maTaiKhoan) {
        return repository.findByMaTaiKhoan(maTaiKhoan);
    }

    public List<BacSi> getByChuyenKhoa(Integer maChuyenKhoa) {
        return repository.findByMaChuyenKhoa(maChuyenKhoa);
    }

    public List<BacSi> getActiveByChuyenKhoa(Integer maChuyenKhoa) {
        return repository.findByMaChuyenKhoaAndTrangThaiTrue(
                maChuyenKhoa
        );
    }

    /**
     * Lay danh sach bac si kem thong tin chi tiet (Chuyen khoa, Email, So dien thoai)
     */
    public List<BacSiChiTietDTO> getDanhSachChiTiet(Integer maChuyenKhoa) {
        List<BacSi> bacSiList;
        if (maChuyenKhoa != null) {
            bacSiList = repository.findByMaChuyenKhoaAndTrangThaiTrue(maChuyenKhoa);
        } else {
            bacSiList = repository.findByTrangThaiTrue();
        }

        List<BacSiChiTietDTO> result = new ArrayList<>();
        for (BacSi bs : bacSiList) {
            String tenChuyenKhoa = "";
            if (bs.getMaChuyenKhoa() != null) {
                Optional<ChuyenKhoa> ckOpt = chuyenKhoaRepository.findById(bs.getMaChuyenKhoa());
                if (ckOpt.isPresent()) {
                    tenChuyenKhoa = ckOpt.get().getTenChuyenKhoa();
                }
            }

            String email = "";
            String sdt = "";
            if (bs.getMaTaiKhoan() != null) {
                Optional<TaiKhoan> tkOpt = taiKhoanRepository.findById(bs.getMaTaiKhoan());
                if (tkOpt.isPresent()) {
                    email = tkOpt.get().getEmail();
                    sdt = tkOpt.get().getSoDienThoai();
                }
            }

            result.add(BacSiChiTietDTO.builder()
                    .maBacSi(bs.getMaBacSi())
                    .maTaiKhoan(bs.getMaTaiKhoan())
                    .tenBacSi(bs.getTenBacSi())
                    .hocVi(bs.getHocVi())
                    .kinhNghiem(bs.getKinhNghiem())
                    .gioiThieu(bs.getGioiThieu())
                    .trangThai(bs.getTrangThai())
                    .maChuyenKhoa(bs.getMaChuyenKhoa())
                    .tenChuyenKhoa(tenChuyenKhoa)
                    .email(email)
                    .soDienThoai(sdt)
                    .build());
        }
        return result;
    }

    /**
     * Admin tao dong thoi TaiKhoan va ho so BacSi
     */
    @Transactional
    public BacSiChiTietDTO taoBacSiMoi(TaoBacSiRequest req) {
        if (taiKhoanRepository.existsByTenDangNhap(req.getTenDangNhap())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại!");
        }
        if (req.getEmail() != null && !req.getEmail().isBlank() && taiKhoanRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại!");
        }

        TaiKhoan tk = new TaiKhoan();
        tk.setTenDangNhap(req.getTenDangNhap());
        tk.setMatKhau(passwordEncoder.encode(req.getMatKhau()));
        tk.setHoTen(req.getHoTen());
        tk.setEmail(req.getEmail());
        tk.setSoDienThoai(req.getSoDienThoai());
        tk.setVaiTro("BacSi");
        tk.setTrangThai(true);
        tk.setNgayTao(LocalDateTime.now());
        TaiKhoan savedTk = taiKhoanRepository.save(tk);

        BacSi bs = new BacSi();
        bs.setMaTaiKhoan(savedTk.getMaTaiKhoan());
        bs.setMaChuyenKhoa(req.getMaChuyenKhoa());
        bs.setTenBacSi(req.getHoTen());
        bs.setHocVi(req.getHocVi());
        bs.setKinhNghiem(req.getKinhNghiem());
        bs.setGioiThieu(req.getGioiThieu());
        bs.setTrangThai(true);
        BacSi savedBs = repository.save(bs);

        String tenChuyenKhoa = "";
        if (req.getMaChuyenKhoa() != null) {
            tenChuyenKhoa = chuyenKhoaRepository.findById(req.getMaChuyenKhoa())
                    .map(ChuyenKhoa::getTenChuyenKhoa)
                    .orElse("");
        }

        return BacSiChiTietDTO.builder()
                .maBacSi(savedBs.getMaBacSi())
                .maTaiKhoan(savedTk.getMaTaiKhoan())
                .tenBacSi(savedBs.getTenBacSi())
                .hocVi(savedBs.getHocVi())
                .kinhNghiem(savedBs.getKinhNghiem())
                .gioiThieu(savedBs.getGioiThieu())
                .trangThai(savedBs.getTrangThai())
                .maChuyenKhoa(savedBs.getMaChuyenKhoa())
                .tenChuyenKhoa(tenChuyenKhoa)
                .email(savedTk.getEmail())
                .soDienThoai(savedTk.getSoDienThoai())
                .build();
    }

    public BacSi save(BacSi bacSi) {
        return repository.save(bacSi);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }
}
