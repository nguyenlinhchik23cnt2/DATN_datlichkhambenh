package quanly.phongkham.repository;

import quanly.phongkham.entity.BenhNhan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BenhNhanRepository
        extends JpaRepository<BenhNhan, Integer> {

    Optional<BenhNhan> findByMaTaiKhoan(Integer maTaiKhoan);
}