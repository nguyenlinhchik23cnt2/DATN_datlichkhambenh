package quanly.phongkham.service;

import quanly.phongkham.entity.TaiKhoan;
import quanly.phongkham.repository.TaiKhoanRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaiKhoanService {

    private final TaiKhoanRepository repository;

    public TaiKhoanService(TaiKhoanRepository repository) {
        this.repository = repository;
    }

    public List<TaiKhoan> getAll() {
        return repository.findAll();
    }

    public Optional<TaiKhoan> getById(Integer id) {
        return repository.findById(id);
    }

    public Optional<TaiKhoan> getByTenDangNhap(String tenDangNhap) {
        return repository.findByTenDangNhap(tenDangNhap);
    }

    public TaiKhoan save(TaiKhoan taiKhoan) {
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
}