package quanly.phongkham.repository;

import quanly.phongkham.entity.TuVanAI;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TuVanAIRepository
        extends JpaRepository<TuVanAI, Integer> {

    List<TuVanAI> findByMaBenhNhan(Integer maBenhNhan);

    List<TuVanAI> findByMaChuyenKhoaGoiY(Integer maChuyenKhoaGoiY);
    List<TuVanAI> findByMaBenhNhanOrderByThoiGianTuVanDesc(Integer maBenhNhan);
}