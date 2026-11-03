package quanly.phongkham.controller;

import quanly.phongkham.entity.DonThuoc;
import quanly.phongkham.service.DonThuocService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/don-thuoc")
@CrossOrigin(origins = "http://localhost:5173")
public class DonThuocController {

    private final DonThuocService service;

    public DonThuocController(DonThuocService service) {
        this.service = service;
    }

    @GetMapping
    public List<DonThuoc> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DonThuoc> getById(
            @PathVariable Integer id) {

        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ket-qua/{maKetQua}")
    public List<DonThuoc> getByKetQua(
            @PathVariable Integer maKetQua) {

        return service.getByKetQua(maKetQua);
    }

    @PostMapping
    public ResponseEntity<DonThuoc> create(
            @RequestBody DonThuoc donThuoc) {

        return ResponseEntity.ok(service.save(donThuoc));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DonThuoc> update(
            @PathVariable Integer id,
            @RequestBody DonThuoc donThuoc) {

        if (service.getById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        donThuoc.setMaDonThuoc(id);
        return ResponseEntity.ok(service.save(donThuoc));
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