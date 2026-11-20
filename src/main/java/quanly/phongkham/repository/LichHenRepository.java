
package quanly.phongkham.repository;

import quanly.phongkham.entity.LichHen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LichHenRepository extends JpaRepository<LichHen, Integer> {

    // Tìm lịch hẹn theo mã đặt lịch
    Optional<LichHen> findByMaDatLich(String maDatLich);

    // Lấy lịch hẹn theo bệnh nhân
    List<LichHen> findByMaBenhNhan(Integer maBenhNhan);

    // Lấy lịch hẹn theo lịch làm việc
    List<LichHen> findByMaLich(Integer maLich);

    // Lấy lịch hẹn theo trạng thái
    List<LichHen> findByTrangThai(String trangThai);

    // Lấy lịch hẹn theo bệnh nhân và trạng thái
    List<LichHen> findByMaBenhNhanAndTrangThai(
            Integer maBenhNhan,
            String trangThai
    );

    // Lấy tất cả lịch hẹn thuộc bác sĩ
    @Query("""
        SELECT lh FROM LichHen lh
        WHERE lh.maLich IN (
            SELECT llv.maLich
            FROM LichLamViec llv
            WHERE llv.maBacSi = :maBacSi
        )
        ORDER BY lh.gioBatDau ASC
    """)
    List<LichHen> findAllLichHenByBacSi(
            @Param("maBacSi") Integer maBacSi
    );

    // Lấy lịch hẹn của bác sĩ theo ngày
    @Query("""
        SELECT lh FROM LichHen lh
        WHERE lh.maLich IN (
            SELECT llv.maLich
            FROM LichLamViec llv
            WHERE llv.maBacSi = :maBacSi
            AND llv.ngayLamViec = :ngay
        )
        ORDER BY lh.gioBatDau ASC
    """)
    List<LichHen> findLichHenByBacSiAndNgay(
            @Param("maBacSi") Integer maBacSi,
            @Param("ngay") LocalDate ngay
    );

    // Lấy lịch hẹn của bác sĩ theo ngày và trạng thái
    @Query("""
        SELECT lh FROM LichHen lh
        WHERE lh.maLich IN (
            SELECT llv.maLich
            FROM LichLamViec llv
            WHERE llv.maBacSi = :maBacSi
            AND llv.ngayLamViec = :ngay
        )
        AND lh.trangThai = :trangThai
        ORDER BY lh.gioBatDau ASC
    """)
    List<LichHen> findLichHenByBacSiAndNgayAndTrangThai(
            @Param("maBacSi") Integer maBacSi,
            @Param("ngay") LocalDate ngay,
            @Param("trangThai") String trangThai
    );

    // Kiểm tra mã đặt lịch đã tồn tại
    boolean existsByMaDatLich(String maDatLich);

    // Lấy lịch hẹn theo lịch làm việc và danh sách trạng thái
    List<LichHen> findByMaLichAndTrangThaiIn(
            Integer maLich,
            List<String> trangThais
    );

    // Kiểm tra khung giờ đã có lịch hẹn hay chưa
    boolean existsByMaLichAndGioBatDauAndTrangThaiIn(
            Integer maLich,
            LocalTime gioBatDau,
            List<String> trangThais
    );

    // Đếm lịch hẹn theo trạng thái
    long countByTrangThai(String trangThai);

    @Query("""
    SELECT COUNT(lh) > 0 FROM LichHen lh
    WHERE lh.maLich = :maLich
      AND lh.trangThai IN :trangThais
      AND lh.gioBatDau < :gioKetThuc
      AND lh.gioKetThuc > :gioBatDau
""")
boolean existsOverlap(@Param("maLich") Integer maLich,
                      @Param("gioBatDau") LocalTime gioBatDau,
                      @Param("gioKetThuc") LocalTime gioKetThuc,
                      @Param("trangThais") List<String> trangThais);

@Query("""
    SELECT lh.gioBatDau FROM LichHen lh
    WHERE lh.maLich = :maLich AND lh.trangThai IN :trangThais
""")
List<LocalTime> findGioDaDat(@Param("maLich") Integer maLich,
                             @Param("trangThais") List<String> trangThais);

@Query("""
    SELECT COUNT(lh) > 0 FROM LichHen lh
    WHERE lh.trangThai IN :trangThais
      AND lh.gioBatDau < :gioKetThuc
      AND lh.gioKetThuc > :gioBatDau
      AND lh.maLich IN (
          SELECT llv.maLich FROM LichLamViec llv
          WHERE llv.maBacSi = :maBacSi AND llv.ngayLamViec = :ngay
      )
""")
boolean existsOverlapTheoBacSiNgay(@Param("maBacSi") Integer maBacSi,
                                   @Param("ngay") LocalDate ngay,
                                   @Param("gioBatDau") LocalTime gioBatDau,
                                   @Param("gioKetThuc") LocalTime gioKetThuc,
                                   @Param("trangThais") List<String> trangThais);

boolean existsByMaLichAndTrangThaiIn(Integer maLich, List<String> trangThais);
}
