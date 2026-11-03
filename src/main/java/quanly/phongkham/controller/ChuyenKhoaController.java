package quanly.phongkham.controller;

import quanly.phongkham.entity.ChuyenKhoa;
import quanly.phongkham.service.ChuyenKhoaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chuyen-khoa")
@CrossOrigin(origins = "http://localhost:5173")
public class ChuyenKhoaController {

    private final ChuyenKhoaService service;

    public ChuyenKhoaController(ChuyenKhoaService service) {
        this.service = service;
    }

    @GetMapping
    public List<ChuyenKhoa> getAll() {
        return service.getAll();
    }

    @GetMapping("/dang-hoat-dong")
    public List<ChuyenKhoa> getActive() {
        return service.getActive();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChuyenKhoa> getById(@PathVariable Integer id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tim-kiem")
    public List<ChuyenKhoa> search(
            @RequestParam String ten) {

        return service.search(ten);
    }

    @PostMapping
    public ResponseEntity<ChuyenKhoa> create(
            @RequestBody ChuyenKhoa chuyenKhoa) {

        return ResponseEntity.ok(service.save(chuyenKhoa));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChuyenKhoa> update(
            @PathVariable Integer id,
            @RequestBody ChuyenKhoa chuyenKhoa) {

        if (service.getById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        chuyenKhoa.setMaChuyenKhoa(id);
        return ResponseEntity.ok(service.save(chuyenKhoa));
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