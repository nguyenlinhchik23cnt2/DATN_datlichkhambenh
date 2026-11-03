package quanly.phongkham.controller;

import quanly.phongkham.entity.ChiTietDonThuoc;
import quanly.phongkham.service.ChiTietDonThuocService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chi-tiet-don-thuoc")
@CrossOrigin(origins = "http://localhost:5173")
public class ChiTietDonThuocController {

    private final ChiTietDonThuocService service;

    public ChiTietDonThuocController(
            ChiTietDonThuocService service) {
        this.service = service;
    }

    @GetMapping
    public List<ChiTietDonThuoc> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChiTietDonThuoc> getById(
            @PathVariable Integer id) {

        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/don-thuoc/{maDonThuoc}")
    public List<ChiTietDonThuoc> getByDonThuoc(
            @PathVariable Integer maDonThuoc) {

        return service.getByDonThuoc(maDonThuoc);
    }

    @PostMapping
    public ResponseEntity<ChiTietDonThuoc> create(
            @RequestBody ChiTietDonThuoc chiTiet) {

        return ResponseEntity.ok(service.save(chiTiet));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChiTietDonThuoc> update(
            @PathVariable Integer id,
            @RequestBody ChiTietDonThuoc chiTiet) {

        if (service.getById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        chiTiet.setMaChiTiet(id);
        return ResponseEntity.ok(service.save(chiTiet));
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