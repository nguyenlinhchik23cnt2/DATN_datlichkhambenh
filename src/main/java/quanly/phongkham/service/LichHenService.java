package quanly.phongkham.service;

import quanly.phongkham.entity.LichHen;
import quanly.phongkham.repository.LichHenRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LichHenService {

    private final LichHenRepository repository;

    public LichHenService(LichHenRepository repository) {
        this.repository = repository;
    }

    public List<LichHen> getAll() {
        return repository.findAll();
    }

    public Optional<LichHen> getById(Integer id) {
        return repository.findById(id);
    }

    public Optional<LichHen> getByMaDatLich(String maDatLich) {
        return repository.findByMaDatLich(maDatLich);
    }

    public List<LichHen> getByBenhNhan(Integer maBenhNhan) {
        return repository.findByMaBenhNhan(maBenhNhan);
    }

    public List<LichHen> getByLich(Integer maLich) {
        return repository.findByMaLich(maLich);
    }

    public List<LichHen> getByTrangThai(String trangThai) {
        return repository.findByTrangThai(trangThai);
    }

    public List<LichHen> getByBenhNhanAndTrangThai(
            Integer maBenhNhan,
            String trangThai) {

        return repository.findByMaBenhNhanAndTrangThai(
                maBenhNhan, trangThai
        );
    }

    public LichHen save(LichHen lichHen) {
        return repository.save(lichHen);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }

    public boolean existsByMaDatLich(String maDatLich) {
        return repository.existsByMaDatLich(maDatLich);
    }
}