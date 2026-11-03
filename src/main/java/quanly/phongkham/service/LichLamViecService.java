package quanly.phongkham.service;

import quanly.phongkham.entity.LichLamViec;
import quanly.phongkham.repository.LichLamViecRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class LichLamViecService {

    private final LichLamViecRepository repository;

    public LichLamViecService(LichLamViecRepository repository) {
        this.repository = repository;
    }

    public List<LichLamViec> getAll() {
        return repository.findAll();
    }

    public Optional<LichLamViec> getById(Integer id) {
        return repository.findById(id);
    }

    public List<LichLamViec> getByBacSi(Integer maBacSi) {
        return repository.findByMaBacSi(maBacSi);
    }

    public List<LichLamViec> getByNgay(LocalDate ngayLamViec) {
        return repository.findByNgayLamViec(ngayLamViec);
    }

    public List<LichLamViec> getByBacSiAndNgay(
            Integer maBacSi,
            LocalDate ngayLamViec) {

        return repository.findByMaBacSiAndNgayLamViec(
                maBacSi, ngayLamViec
        );
    }

    public List<LichLamViec> getAvailableSchedule(
            Integer maBacSi,
            LocalDate ngayLamViec) {

        return repository.findByMaBacSiAndNgayLamViecAndTrangThaiTrue(
                maBacSi, ngayLamViec
        );
    }

    public LichLamViec save(LichLamViec lichLamViec) {
        return repository.save(lichLamViec);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }
}