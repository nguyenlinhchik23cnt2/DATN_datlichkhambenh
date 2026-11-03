package quanly.phongkham.repository;

import quanly.phongkham.entity.BacSi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BacSiRepository
        extends JpaRepository<BacSi, Integer> {

    Optional<BacSi> findByMaTaiKhoan(Integer maTaiKhoan);

    List<BacSi> findByMaChuyenKhoa(Integer maChuyenKhoa);

    List<BacSi> findByTrangThaiTrue();

    List<BacSi> findByMaChuyenKhoaAndTrangThaiTrue(
            Integer maChuyenKhoa
    );
}