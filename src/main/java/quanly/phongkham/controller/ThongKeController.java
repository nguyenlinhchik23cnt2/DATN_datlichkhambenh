package quanly.phongkham.controller;

import quanly.phongkham.dto.ThongKeChuyenKhoaDTO;
import quanly.phongkham.dto.ThongKeTheoThangDTO;
import quanly.phongkham.dto.ThongKeTongQuanDTO;
import quanly.phongkham.service.ThongKeService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;

@RestController
@RequestMapping("/api/thong-ke")
@CrossOrigin(origins = "http://localhost:5173")
public class ThongKeController {

    private final ThongKeService service;

    public ThongKeController(ThongKeService service) {
        this.service = service;
    }

    @GetMapping("/tong-quan")
    public ResponseEntity<ThongKeTongQuanDTO> getTongQuan() {
        return ResponseEntity.ok(service.getTongQuan());
    }

    @GetMapping("/theo-thang")
    public ResponseEntity<List<ThongKeTheoThangDTO>> getThongKeTheoThang(
            @RequestParam(required = false) Integer nam) {
        int targetYear = (nam != null) ? nam : Year.now().getValue();
        return ResponseEntity.ok(service.getThongKeTheoThang(targetYear));
    }

    @GetMapping("/theo-chuyen-khoa")
    public ResponseEntity<List<ThongKeChuyenKhoaDTO>> getThongKeTheoChuyenKhoa(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {
        return ResponseEntity.ok(service.getThongKeTheoChuyenKhoa(tuNgay, denNgay));
    }
}

