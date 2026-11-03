package quanly.phongkham.repository;

import quanly.phongkham.entity.LichLamViec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LichLamViecRepository
        extends JpaRepository<LichLamViec, Integer> {

    List<LichLamViec> findByMaBacSi(Integer maBacSi);

    List<LichLamViec> findByNgayLamViec(LocalDate ngayLamViec);

    List<LichLamViec> findByMaBacSiAndNgayLamViec(
            Integer maBacSi,
            LocalDate ngayLamViec
    );

    List<LichLamViec> findByMaBacSiAndNgayLamViecAndTrangThaiTrue(
            Integer maBacSi,
            LocalDate ngayLamViec
    );
}