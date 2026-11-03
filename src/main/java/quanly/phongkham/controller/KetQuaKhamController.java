package quanly.phongkham.controller;

import quanly.phongkham.entity.KetQuaKham;
import quanly.phongkham.service.KetQuaKhamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ket-qua-kham")
@CrossOrigin(origins = "http://localhost:5173")
public class KetQuaKhamController {

    private final KetQuaKhamService service;

    public KetQuaKhamController(KetQuaKhamService service) {
        this.service = service;
    }

    @GetMapping
    public List<KetQuaKham> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<KetQuaKham> getById(
            @PathVariable Integer id) {

        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/lich-hen/{maLichHen}")
    public List<KetQuaKham> getByLichHen(
            @PathVariable Integer maLichHen) {

        return service.getAllByLichHen(maLichHen);
    }

    @PostMapping
    public ResponseEntity<KetQuaKham> create(
            @RequestBody KetQuaKham ketQuaKham) {

        return ResponseEntity.ok(service.save(ketQuaKham));
    }

    @PutMapping("/{id}")
    public ResponseEntity<KetQuaKham> update(
            @PathVariable Integer id,
            @RequestBody KetQuaKham ketQuaKham) {

        if (service.getById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ketQuaKham.setMaKetQua(id);
        return ResponseEntity.ok(service.save(ketQuaKham));
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