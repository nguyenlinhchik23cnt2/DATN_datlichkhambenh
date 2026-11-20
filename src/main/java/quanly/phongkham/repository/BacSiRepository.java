package quanly.phongkham.repository;

import jakarta.persistence.LockModeType;
import quanly.phongkham.entity.BacSi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BacSiRepository extends JpaRepository<BacSi, Integer> {

    List<BacSi> findByTrangThaiTrue();

    Optional<BacSi> findByMaTaiKhoan(Integer maTaiKhoan);

    List<BacSi> findByMaChuyenKhoa(Integer maChuyenKhoa);

    List<BacSi> findByMaChuyenKhoaAndTrangThaiTrue(Integer maChuyenKhoa);

    // Khóa dòng bác sĩ khi đặt lịch / tạo ca mặc định (chống đặt trùng khi bấm cùng lúc)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BacSi b WHERE b.maBacSi = :id")
    Optional<BacSi> findByIdForUpdate(@Param("id") Integer id);
}