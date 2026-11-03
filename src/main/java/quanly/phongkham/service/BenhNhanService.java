package quanly.phongkham.service;

import quanly.phongkham.entity.BenhNhan;
import quanly.phongkham.repository.BenhNhanRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BenhNhanService {

    private final BenhNhanRepository repository;

    public BenhNhanService(BenhNhanRepository repository) {
        this.repository = repository;
    }

    public List<BenhNhan> getAll() {
        return repository.findAll();
    }

    public Optional<BenhNhan> getById(Integer id) {
        return repository.findById(id);
    }

    public Optional<BenhNhan> getByMaTaiKhoan(Integer maTaiKhoan) {
        return repository.findByMaTaiKhoan(maTaiKhoan);
    }

    public BenhNhan save(BenhNhan benhNhan) {
        return repository.save(benhNhan);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }
}