package quanly.phongkham.repository;

import quanly.phongkham.entity.ChuyenKhoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChuyenKhoaRepository
        extends JpaRepository<ChuyenKhoa, Integer> {

    List<ChuyenKhoa> findByTrangThaiTrue();

    List<ChuyenKhoa> findByTenChuyenKhoaContainingIgnoreCase(
            String tenChuyenKhoa
    );
    
}