package quanly.phongkham.controller;

import quanly.phongkham.entity.TuVanAI;
import quanly.phongkham.service.TuVanAIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tu-van-ai")
@CrossOrigin(origins = "http://localhost:5173")
public class TuVanAIController {

    private final TuVanAIService service;

    public TuVanAIController(TuVanAIService service) {
        this.service = service;
    }

    @GetMapping
    public List<TuVanAI> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TuVanAI> getById(
            @PathVariable Integer id) {

        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/benh-nhan/{maBenhNhan}")
    public List<TuVanAI> getByBenhNhan(
            @PathVariable Integer maBenhNhan) {

        return service.getByBenhNhan(maBenhNhan);
    }

    @GetMapping("/chuyen-khoa/{maChuyenKhoa}")
    public List<TuVanAI> getByChuyenKhoa(
            @PathVariable Integer maChuyenKhoa) {

        return service.getByChuyenKhoaGoiY(maChuyenKhoa);
    }

    @PostMapping
    public ResponseEntity<TuVanAI> create(
            @RequestBody TuVanAI tuVanAI) {

        return ResponseEntity.ok(service.save(tuVanAI));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TuVanAI> update(
            @PathVariable Integer id,
            @RequestBody TuVanAI tuVanAI) {

        if (service.getById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        tuVanAI.setMaTuVan(id);
        return ResponseEntity.ok(service.save(tuVanAI));
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