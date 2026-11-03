package quanly.phongkham.repository;

import quanly.phongkham.entity.DonThuoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonThuocRepository
        extends JpaRepository<DonThuoc, Integer> {

    List<DonThuoc> findByMaKetQua(Integer maKetQua);
}