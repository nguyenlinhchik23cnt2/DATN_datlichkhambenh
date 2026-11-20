package quanly.phongkham.service;

import quanly.phongkham.dto.KhungGioSlotDTO;
import quanly.phongkham.entity.LichLamViec;
import quanly.phongkham.repository.BacSiRepository;
import quanly.phongkham.repository.LichHenRepository;
import quanly.phongkham.repository.LichLamViecRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LichLamViecService {

    // Các trạng thái được tính là "đang chiếm giờ" (phải giống LichHenService)
    private static final List<String> TRANG_THAI_CHIEM_GIO =
            List.of("ChoDuyet", "DaDuyet", "HoanThanh");

    private static final int THOI_LUONG_PHUT = 30;

    private final LichLamViecRepository repository;
    private final LichHenRepository lichHenRepository;
    private final BacSiRepository bacSiRepository;

    public LichLamViecService(
            LichLamViecRepository repository,
            LichHenRepository lichHenRepository,
            BacSiRepository bacSiRepository) {

        this.repository = repository;
        this.lichHenRepository = lichHenRepository;
        this.bacSiRepository = bacSiRepository;
    }

    public List<LichLamViec> getAll() {
        return repository.findAll();
    }

    public Optional<LichLamViec> getById(Integer id) {
        return repository.findById(id);
    }

    public List<LichLamViec> getByBacSi(Integer maBacSi) {
        return repository.findByMaBacSi(maBacSi);
    }

    public List<LichLamViec> getByNgay(LocalDate ngayLamViec) {
        return repository.findByNgayLamViec(ngayLamViec);
    }

    public List<LichLamViec> getByBacSiAndNgay(
            Integer maBacSi,
            LocalDate ngayLamViec) {

        return repository.findByMaBacSiAndNgayLamViec(
                maBacSi, ngayLamViec
        );
    }

    public List<LichLamViec> getByBacSiAndKhoangNgay(
            Integer maBacSi,
            LocalDate tuNgay,
            LocalDate denNgay) {

        return repository.findByMaBacSiAndNgayLamViecBetween(
                maBacSi, tuNgay, denNgay
        );
    }

    public List<LichLamViec> getAvailableSchedule(
            Integer maBacSi,
            LocalDate ngayLamViec) {

        return repository.findByMaBacSiAndNgayLamViecAndTrangThaiTrue(
                maBacSi, ngayLamViec
        );
    }

    /**
     * Tính danh sách slot 30 phút của một bác sĩ trong ngày.
     * Nếu bác sĩ CHƯA CÓ ca nào trong ngày, tự tạo ca mặc định
     * (sáng 08:00-11:30, chiều 13:30-17:00).
     *
     * Việc tạo ca được bảo vệ bằng khóa dòng bác sĩ + kiểm tra lại,
     * nên nhiều request cùng lúc chỉ tạo ca một lần.
     */
    @Transactional
    public List<KhungGioSlotDTO> getAvailableSlots(
            Integer maBacSi,
            LocalDate ngayLamViec) {

        if (maBacSi == null || ngayLamViec == null) {
            return new ArrayList<>();
        }

        List<LichLamViec> caLamViecList =
                repository.findByMaBacSiAndNgayLamViecAndTrangThaiTrue(
                        maBacSi, ngayLamViec
                );

        if (caLamViecList.isEmpty()) {
            caLamViecList = taoCaMacDinhNeuChuaCo(maBacSi, ngayLamViec);
        }

        List<KhungGioSlotDTO> result = new ArrayList<>();

        for (LichLamViec ca : caLamViecList) {

            LocalTime current = ca.getGioBatDau();
            LocalTime end = ca.getGioKetThuc();

            while (!current.plusMinutes(THOI_LUONG_PHUT).isAfter(end)) {

                LocalTime slotStart = current;
                LocalTime slotEnd = current.plusMinutes(THOI_LUONG_PHUT);

                // Kiểm tra theo bác sĩ + ngày, giống lúc đặt lịch
                boolean daDat = lichHenRepository.existsOverlapTheoBacSiNgay(
                        maBacSi,
                        ngayLamViec,
                        slotStart,
                        slotEnd,
                        TRANG_THAI_CHIEM_GIO
                );

                result.add(KhungGioSlotDTO.builder()
                        .maLich(ca.getMaLich())
                        .gioBatDau(slotStart)
                        .gioKetThuc(slotEnd)
                        .conTrong(!daDat)
                        .build());

                current = slotEnd;
            }
        }

        return result;
    }

    /**
     * Khóa dòng bác sĩ, kiểm tra lại, rồi mới tạo ca mặc định.
     * Chỉ tạo khi bác sĩ không có ca nào trong ngày (kể cả ca đã tắt).
     */
    private List<LichLamViec> taoCaMacDinhNeuChuaCo(
            Integer maBacSi,
            LocalDate ngayLamViec) {

        // Không có bác sĩ thì không tạo ca
        if (bacSiRepository.findByIdForUpdate(maBacSi).isEmpty()) {
            return new ArrayList<>();
        }

        // Kiểm tra lại sau khi giữ khóa: request khác có thể vừa tạo xong
        List<LichLamViec> tatCaCa =
                repository.findByMaBacSiAndNgayLamViec(maBacSi, ngayLamViec);

        if (!tatCaCa.isEmpty()) {
            // Đã có ca (do request khác tạo, hoặc ca đang bị tắt)
            return repository.findByMaBacSiAndNgayLamViecAndTrangThaiTrue(
                    maBacSi, ngayLamViec
            );
        }

        LichLamViec caSang = new LichLamViec();
        caSang.setMaBacSi(maBacSi);
        caSang.setNgayLamViec(ngayLamViec);
        caSang.setGioBatDau(LocalTime.of(8, 0));
        caSang.setGioKetThuc(LocalTime.of(11, 30));
        caSang.setTrangThai(true);

        LichLamViec caChieu = new LichLamViec();
        caChieu.setMaBacSi(maBacSi);
        caChieu.setNgayLamViec(ngayLamViec);
        caChieu.setGioBatDau(LocalTime.of(13, 30));
        caChieu.setGioKetThuc(LocalTime.of(17, 0));
        caChieu.setTrangThai(true);

        return new ArrayList<>(repository.saveAll(List.of(caSang, caChieu)));
    }

    public LichLamViec save(LichLamViec lichLamViec) {
        return repository.save(lichLamViec);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }
}