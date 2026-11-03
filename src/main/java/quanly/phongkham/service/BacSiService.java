package quanly.phongkham.service;

import quanly.phongkham.entity.BacSi;
import quanly.phongkham.repository.BacSiRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BacSiService {

    private final BacSiRepository repository;

    public BacSiService(BacSiRepository repository) {
        this.repository = repository;
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

    public BacSi save(BacSi bacSi) {
        return repository.save(bacSi);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }
}