package quanly.phongkham.controller;

import quanly.phongkham.dto.BacSiChiTietDTO;
import quanly.phongkham.dto.TaoBacSiRequest;
import quanly.phongkham.entity.BacSi;
import quanly.phongkham.service.BacSiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bac-si")
@CrossOrigin(origins = "http://localhost:5173")
public class BacSiController {

    private final BacSiService service;

    public BacSiController(BacSiService service) {
        this.service = service;
    }

    @GetMapping
    public List<BacSi> getAll() {
        return service.getAll();
    }

    @GetMapping("/danh-sach-chi-tiet")
    public List<BacSiChiTietDTO> getDanhSachChiTiet(
            @RequestParam(required = false) Integer maChuyenKhoa) {
        return service.getDanhSachChiTiet(maChuyenKhoa);
    }

    @GetMapping("/dang-hoat-dong")
    public List<BacSi> getActive() {
        return service.getActive();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BacSi> getById(@PathVariable Integer id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tai-khoan/{maTaiKhoan}")
    public ResponseEntity<BacSi> getByTaiKhoan(
            @PathVariable Integer maTaiKhoan) {

        return service.getByMaTaiKhoan(maTaiKhoan)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/chuyen-khoa/{maChuyenKhoa}")
    public List<BacSi> getByChuyenKhoa(
            @PathVariable Integer maChuyenKhoa) {

        return service.getByChuyenKhoa(maChuyenKhoa);
    }

    @GetMapping("/chuyen-khoa/{maChuyenKhoa}/dang-hoat-dong")
    public List<BacSi> getActiveByChuyenKhoa(
            @PathVariable Integer maChuyenKhoa) {

        return service.getActiveByChuyenKhoa(maChuyenKhoa);
    }

    @PostMapping("/tao-moi")
    public ResponseEntity<?> taoBacSiMoi(@Valid @RequestBody TaoBacSiRequest req) {
        try {
            BacSiChiTietDTO result = service.taoBacSiMoi(req);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<BacSi> create(@RequestBody BacSi bacSi) {
        return ResponseEntity.ok(service.save(bacSi));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BacSi> update(
            @PathVariable Integer id,
            @RequestBody BacSi bacSi) {

        if (service.getById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        bacSi.setMaBacSi(id);
        return ResponseEntity.ok(service.save(bacSi));
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
