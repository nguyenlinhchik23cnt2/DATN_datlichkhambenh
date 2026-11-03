package quanly.phongkham.repository;

import quanly.phongkham.entity.LichHen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LichHenRepository
        extends JpaRepository<LichHen, Integer> {

    Optional<LichHen> findByMaDatLich(String maDatLich);

    List<LichHen> findByMaBenhNhan(Integer maBenhNhan);

    List<LichHen> findByMaLich(Integer maLich);

    List<LichHen> findByTrangThai(String trangThai);

    List<LichHen> findByMaBenhNhanAndTrangThai(
            Integer maBenhNhan,
            String trangThai
    );

    boolean existsByMaDatLich(String maDatLich);
}