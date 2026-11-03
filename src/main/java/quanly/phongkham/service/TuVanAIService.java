package quanly.phongkham.service;

import quanly.phongkham.entity.TuVanAI;
import quanly.phongkham.repository.TuVanAIRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TuVanAIService {

    private final TuVanAIRepository repository;

    public TuVanAIService(TuVanAIRepository repository) {
        this.repository = repository;
    }

    public List<TuVanAI> getAll() {
        return repository.findAll();
    }

    public Optional<TuVanAI> getById(Integer id) {
        return repository.findById(id);
    }

    public List<TuVanAI> getByBenhNhan(Integer maBenhNhan) {
        return repository.findByMaBenhNhan(maBenhNhan);
    }

    public List<TuVanAI> getByChuyenKhoaGoiY(Integer maChuyenKhoa) {
        return repository.findByMaChuyenKhoaGoiY(maChuyenKhoa);
    }

    public TuVanAI save(TuVanAI tuVanAI) {
        return repository.save(tuVanAI);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }
}