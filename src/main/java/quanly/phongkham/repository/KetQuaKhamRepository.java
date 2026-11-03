package quanly.phongkham.repository;

import quanly.phongkham.entity.KetQuaKham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KetQuaKhamRepository
        extends JpaRepository<KetQuaKham, Integer> {

    Optional<KetQuaKham> findByMaLichHen(Integer maLichHen);

    List<KetQuaKham> findAllByMaLichHen(Integer maLichHen);
}