package quanly.phongkham.repository;

import jakarta.persistence.LockModeType;
import quanly.phongkham.entity.LichLamViec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    List<LichLamViec> findByMaBacSiAndNgayLamViecBetween(
            Integer maBacSi,
            LocalDate tuNgay,
            LocalDate denNgay
    );

    List<LichLamViec> findByNgayLamViecBetween(
            LocalDate tuNgay,
            LocalDate denNgay
    );

    // Khóa dòng ca làm việc khi đặt lịch (chống đặt trùng khi nhiều người bấm cùng lúc)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM LichLamViec l WHERE l.maLich = :id")
    Optional<LichLamViec> findByIdForUpdate(@Param("id") Integer id);
}