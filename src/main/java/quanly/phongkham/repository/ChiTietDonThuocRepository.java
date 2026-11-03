package quanly.phongkham.repository;

import quanly.phongkham.entity.ChiTietDonThuoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietDonThuocRepository
        extends JpaRepository<ChiTietDonThuoc, Integer> {

    List<ChiTietDonThuoc> findByMaDonThuoc(Integer maDonThuoc);
}