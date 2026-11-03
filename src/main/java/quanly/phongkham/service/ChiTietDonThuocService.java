package quanly.phongkham.service;

import quanly.phongkham.entity.ChiTietDonThuoc;
import quanly.phongkham.repository.ChiTietDonThuocRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChiTietDonThuocService {

    private final ChiTietDonThuocRepository repository;

    public ChiTietDonThuocService(
            ChiTietDonThuocRepository repository) {
        this.repository = repository;
    }

    public List<ChiTietDonThuoc> getAll() {
        return repository.findAll();
    }

    public Optional<ChiTietDonThuoc> getById(Integer id) {
        return repository.findById(id);
    }

    public List<ChiTietDonThuoc> getByDonThuoc(Integer maDonThuoc) {
        return repository.findByMaDonThuoc(maDonThuoc);
    }

    public ChiTietDonThuoc save(ChiTietDonThuoc chiTiet) {
        return repository.save(chiTiet);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }
}