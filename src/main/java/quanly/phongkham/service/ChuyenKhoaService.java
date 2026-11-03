package quanly.phongkham.service;

import quanly.phongkham.entity.ChuyenKhoa;
import quanly.phongkham.repository.ChuyenKhoaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChuyenKhoaService {

    private final ChuyenKhoaRepository repository;

    public ChuyenKhoaService(ChuyenKhoaRepository repository) {
        this.repository = repository;
    }

    public List<ChuyenKhoa> getAll() {
        return repository.findAll();
    }

    public List<ChuyenKhoa> getActive() {
        return repository.findByTrangThaiTrue();
    }

    public Optional<ChuyenKhoa> getById(Integer id) {
        return repository.findById(id);
    }

    public List<ChuyenKhoa> search(String tenChuyenKhoa) {
        return repository.findByTenChuyenKhoaContainingIgnoreCase(
                tenChuyenKhoa
        );
    }

    public ChuyenKhoa save(ChuyenKhoa chuyenKhoa) {
        return repository.save(chuyenKhoa);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }
}