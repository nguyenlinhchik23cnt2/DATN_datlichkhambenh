package quanly.phongkham.service;

import quanly.phongkham.entity.DonThuoc;
import quanly.phongkham.repository.DonThuocRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DonThuocService {

    private final DonThuocRepository repository;

    public DonThuocService(DonThuocRepository repository) {
        this.repository = repository;
    }

    public List<DonThuoc> getAll() {
        return repository.findAll();
    }

    public Optional<DonThuoc> getById(Integer id) {
        return repository.findById(id);
    }

    public List<DonThuoc> getByKetQua(Integer maKetQua) {
        return repository.findByMaKetQua(maKetQua);
    }

    public DonThuoc save(DonThuoc donThuoc) {
        return repository.save(donThuoc);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }
}