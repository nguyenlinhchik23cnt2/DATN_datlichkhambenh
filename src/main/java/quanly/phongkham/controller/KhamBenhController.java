package quanly.phongkham.controller;

import quanly.phongkham.dto.KhamBenhRequest;
import quanly.phongkham.service.KhamBenhService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/kham-benh")
@CrossOrigin(origins = "http://localhost:5173")
public class KhamBenhController {

    private final KhamBenhService service;

    public KhamBenhController(KhamBenhService service) {
        this.service = service;
    }

    @PostMapping("/hoan-thanh")
    public ResponseEntity<?> hoanThanhKhamBenh(@Valid @RequestBody KhamBenhRequest request) {
        try {
            Map<String, Object> result = service.hoanThanhKhamBenh(request);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}

