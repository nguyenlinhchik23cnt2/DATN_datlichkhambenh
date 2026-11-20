package quanly.phongkham.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import quanly.phongkham.dto.TuVanAIDto.PhanTichRequest;
import quanly.phongkham.service.TuVanAIService;

import java.util.Map;

@RestController
@RequestMapping("/api/tu-van-ai")
public class TuVanAIController {

    private final TuVanAIService service;

    public TuVanAIController(TuVanAIService service) {
        this.service = service;
    }

    // Công khai (permitAll) - widget chat gọi endpoint này
    @PostMapping("/phan-tich")
    public ResponseEntity<?> phanTich(@RequestBody PhanTichRequest request) {
        try {
            return ResponseEntity.ok(service.phanTich(request.noiDungTrieuChung()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // Bệnh nhân xem lại lịch sử của mình
    @GetMapping("/cua-toi")
    public ResponseEntity<?> cuaToi() {
        return ResponseEntity.ok(service.lichSuCuaToi());
    }
}