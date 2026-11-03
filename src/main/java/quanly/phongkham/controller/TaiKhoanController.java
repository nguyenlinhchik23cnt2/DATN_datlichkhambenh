package quanly.phongkham.controller;

import quanly.phongkham.entity.TaiKhoan;
import quanly.phongkham.service.TaiKhoanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tai-khoan")
@CrossOrigin(origins = "http://localhost:5173")
public class TaiKhoanController {

    private final TaiKhoanService service;

    public TaiKhoanController(TaiKhoanService service) {
        this.service = service;
    }

    @GetMapping
    public List<TaiKhoan> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaiKhoan> getById(@PathVariable Integer id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ten-dang-nhap/{tenDangNhap}")
    public ResponseEntity<TaiKhoan> getByTenDangNhap(
            @PathVariable String tenDangNhap) {

        return service.getByTenDangNhap(tenDangNhap)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TaiKhoan> create(
            @RequestBody TaiKhoan taiKhoan) {

        return ResponseEntity.ok(service.save(taiKhoan));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaiKhoan> update(
            @PathVariable Integer id,
            @RequestBody TaiKhoan taiKhoan) {

        if (service.getById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        taiKhoan.setMaTaiKhoan(id);
        return ResponseEntity.ok(service.save(taiKhoan));
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