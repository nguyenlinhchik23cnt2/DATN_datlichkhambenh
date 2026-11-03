package quanly.phongkham.service;

import quanly.phongkham.entity.KetQuaKham;
import quanly.phongkham.repository.KetQuaKhamRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class KetQuaKhamService {

    private final KetQuaKhamRepository repository;

    public KetQuaKhamService(KetQuaKhamRepository repository) {
        this.repository = repository;
    }

    public List<KetQuaKham> getAll() {
        return repository.findAll();
    }

    public Optional<KetQuaKham> getById(Integer id) {
        return repository.findById(id);
    }

    public Optional<KetQuaKham> getByLichHen(Integer maLichHen) {
        return repository.findByMaLichHen(maLichHen);
    }

    public List<KetQuaKham> getAllByLichHen(Integer maLichHen) {
        return repository.findAllByMaLichHen(maLichHen);
    }

    public KetQuaKham save(KetQuaKham ketQuaKham) {
        return repository.save(ketQuaKham);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }
}