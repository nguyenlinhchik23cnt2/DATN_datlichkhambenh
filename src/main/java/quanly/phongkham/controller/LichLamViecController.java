package quanly.phongkham.controller;

import quanly.phongkham.entity.LichLamViec;
import quanly.phongkham.service.LichLamViecService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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
            @PathVariable @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE
            ) LocalDate ngay) {

        return service.getByNgay(ngay);
    }

    @GetMapping("/bac-si/{maBacSi}/ngay/{ngay}")
    public List<LichLamViec> getByBacSiAndNgay(
            @PathVariable Integer maBacSi,
            @PathVariable @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE
            ) LocalDate ngay) {

        return service.getByBacSiAndNgay(maBacSi, ngay);
    }

    @GetMapping("/bac-si/{maBacSi}/ngay/{ngay}/con-trong")
    public List<LichLamViec> getAvailable(
            @PathVariable Integer maBacSi,
            @PathVariable @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE
            ) LocalDate ngay) {

        return service.getAvailableSchedule(maBacSi, ngay);
    }

    @PostMapping
    public ResponseEntity<LichLamViec> create(
            @RequestBody LichLamViec lichLamViec) {

        return ResponseEntity.ok(service.save(lichLamViec));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LichLamViec> update(
            @PathVariable Integer id,
            @RequestBody LichLamViec lichLamViec) {

        if (service.getById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        lichLamViec.setMaLich(id);
        return ResponseEntity.ok(service.save(lichLamViec));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (service.getById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}