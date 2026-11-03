package quanly.phongkham.controller;

import quanly.phongkham.entity.LichHen;
import quanly.phongkham.service.LichHenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lich-hen")
@CrossOrigin(origins = "http://localhost:5173")
public class LichHenController {

    private final LichHenService service;

    public LichHenController(LichHenService service) {
        this.service = service;
    }

    @GetMapping
    public List<LichHen> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LichHen> getById(
            @PathVariable Integer id) {

        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ma-dat-lich/{maDatLich}")
    public ResponseEntity<LichHen> getByMaDatLich(
            @PathVariable String maDatLich) {

        return service.getByMaDatLich(maDatLich)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/benh-nhan/{maBenhNhan}")
    public List<LichHen> getByBenhNhan(
            @PathVariable Integer maBenhNhan) {

        return service.getByBenhNhan(maBenhNhan);
    }

    @GetMapping("/lich-lam-viec/{maLich}")
    public List<LichHen> getByLich(
            @PathVariable Integer maLich) {

        return service.getByLich(maLich);
    }

    @GetMapping("/trang-thai/{trangThai}")
    public List<LichHen> getByTrangThai(
            @PathVariable String trangThai) {

        return service.getByTrangThai(trangThai);
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody LichHen lichHen) {

        if (service.existsByMaDatLich(lichHen.getMaDatLich())) {
            return ResponseEntity.badRequest()
                    .body("Mã đặt lịch đã tồn tại!");
        }

        return ResponseEntity.ok(service.save(lichHen));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LichHen> update(
            @PathVariable Integer id,
            @RequestBody LichHen lichHen) {

        if (service.getById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        lichHen.setMaLichHen(id);
        return ResponseEntity.ok(service.save(lichHen));
    }

    @PatchMapping("/{id}/trang-thai")
    public ResponseEntity<?> updateTrangThai(
            @PathVariable Integer id,
            @RequestParam String trangThai) {

        var lichHen = service.getById(id);

        if (lichHen.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        LichHen item = lichHen.get();
        item.setTrangThai(trangThai);

        return ResponseEntity.ok(service.save(item));
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