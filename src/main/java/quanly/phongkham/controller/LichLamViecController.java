package quanly.phongkham.controller;

import quanly.phongkham.dto.KhungGioSlotDTO;
import quanly.phongkham.entity.LichLamViec;
import quanly.phongkham.service.LichLamViecService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lich-lam-viec")
@CrossOrigin(origins = "http://localhost:5173")
public class LichLamViecController {

    private final LichLamViecService service;

    public LichLamViecController(LichLamViecService service) {
        this.service = service;
    }

    @GetMapping
    public List<LichLamViec> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LichLamViec> getById(
            @PathVariable Integer id) {

        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/bac-si/{maBacSi}")
    public List<LichLamViec> getByBacSi(
            @PathVariable Integer maBacSi) {

        return service.getByBacSi(maBacSi);
    }

    @GetMapping("/ngay/{ngay}")
    public List<LichLamViec> getByNgay(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay) {

        return service.getByNgay(ngay);
    }

    @GetMapping("/bac-si/{maBacSi}/ngay/{ngay}")
    public List<LichLamViec> getByBacSiAndNgay(
            @PathVariable Integer maBacSi,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay) {

        return service.getByBacSiAndNgay(maBacSi, ngay);
    }

    @GetMapping("/bac-si/{maBacSi}/khoang-ngay")
    public List<LichLamViec> getByBacSiAndKhoangNgay(
            @PathVariable Integer maBacSi,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {

        return service.getByBacSiAndKhoangNgay(maBacSi, tuNgay, denNgay);
    }

    @GetMapping("/bac-si/{maBacSi}/slots")
    public List<KhungGioSlotDTO> getSlots(
            @PathVariable Integer maBacSi,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay) {

        return service.getAvailableSlots(maBacSi, ngay);
    }

    @GetMapping("/bac-si/{maBacSi}/ngay/{ngay}/con-trong")
    public List<LichLamViec> getAvailable(
            @PathVariable Integer maBacSi,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay) {

        return service.getAvailableSchedule(maBacSi, ngay);
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody LichLamViec lichLamViec) {

        try {
            // Tạo mới: bỏ mã để chắc chắn không ghi đè ca có sẵn
            lichLamViec.setMaLich(null);
            return ResponseEntity.ok(service.save(lichLamViec));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message",
                            "Ca làm việc này đã tồn tại cho bác sĩ trong ngày!"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @RequestBody LichLamViec lichLamViec) {

        if (service.getById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            lichLamViec.setMaLich(id);
            return ResponseEntity.ok(service.save(lichLamViec));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message",
                            "Ca làm việc này đã tồn tại cho bác sĩ trong ngày!"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {

        if (service.getById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }
}