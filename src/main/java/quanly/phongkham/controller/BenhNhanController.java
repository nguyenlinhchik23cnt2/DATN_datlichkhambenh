package quanly.phongkham.controller;

import quanly.phongkham.entity.BenhNhan;
import quanly.phongkham.service.BenhNhanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/benh-nhan")
@CrossOrigin(origins = "http://localhost:5173")
public class BenhNhanController {

    private final BenhNhanService service;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private quanly.phongkham.service.TaiKhoanService taiKhoanService;

    public BenhNhanController(BenhNhanService service) {
        this.service = service;
    }

    @PostMapping("/dang-ky")
    public ResponseEntity<?> dangKy(
            @jakarta.validation.Valid @RequestBody quanly.phongkham.dto.DangKyBenhNhanRequest request) {
        if (taiKhoanService == null) {
            return ResponseEntity.status(500).body(java.util.Map.of("message", "Dịch vụ đăng ký tạm thời không khả dụng!"));
        }
        try {
            return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                    .body(taiKhoanService.dangKyBenhNhan(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    public List<BenhNhan> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BenhNhan> getById(@PathVariable Integer id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tai-khoan/{maTaiKhoan}")
    public ResponseEntity<BenhNhan> getByTaiKhoan(
            @PathVariable Integer maTaiKhoan) {

        return service.getByMaTaiKhoan(maTaiKhoan)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BenhNhan> create(
            @RequestBody BenhNhan benhNhan) {

        return ResponseEntity.ok(service.save(benhNhan));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BenhNhan> update(
            @PathVariable Integer id,
            @RequestBody BenhNhan benhNhan) {

        if (service.getById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        benhNhan.setMaBenhNhan(id);
        return ResponseEntity.ok(service.save(benhNhan));
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