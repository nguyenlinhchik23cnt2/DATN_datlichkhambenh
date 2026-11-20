package quanly.phongkham.controller;

import quanly.phongkham.config.NguoiDungHienTai;
import quanly.phongkham.dto.DatLichHenRequest;
import quanly.phongkham.entity.LichHen;
import quanly.phongkham.service.LichHenService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/lich-hen")
@CrossOrigin(origins = "http://localhost:5173")
public class LichHenController {

    // Múi giờ Việt Nam: tránh lệch ngày khi server chạy UTC
    private static final ZoneId ZONE_VN = ZoneId.of("Asia/Ho_Chi_Minh");

    private final LichHenService service;
    private final NguoiDungHienTai nguoiDung;
    
    public LichHenController(LichHenService service, NguoiDungHienTai nguoiDung) {
        this.service = service;
        this.nguoiDung = nguoiDung;
    }

    // Tiếp tân / Admin xem toàn bộ lịch (SecurityConfig đã giới hạn quyền)
    @GetMapping
    public List<LichHen> getAll() {
        return service.getAll();
    }

    /**
     * Bác sĩ chỉ xem lịch hẹn của chính mình.
     * Nếu không truyền ngày thì mặc định lấy ngày hiện tại (giờ Việt Nam).
     */
    @GetMapping("/bac-si-cua-toi")
    public ResponseEntity<?> getLichBacSiCuaToi(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay) {

        if (!nguoiDung.laBacSi()) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Chỉ bác sĩ mới được sử dụng chức năng này!"));
        }

        Integer maBacSi = nguoiDung.maBacSi().orElse(null);

        if (maBacSi == null) {
            return ResponseEntity.status(403)
                    .body(Map.of("message",
                            "Không tìm thấy hồ sơ bác sĩ của tài khoản đang đăng nhập!"));
        }

        if (ngay == null) {
            ngay = LocalDate.now(ZONE_VN);
        }

        return ResponseEntity.ok(service.getLichHenBacSiTheoNgay(maBacSi, ngay));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LichHen> getById(@PathVariable Integer id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ma-dat-lich/{maDatLich}")
    public ResponseEntity<LichHen> getByMaDatLich(@PathVariable String maDatLich) {
        return service.getByMaDatLich(maDatLich)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/benh-nhan/{maBenhNhan}")
    public ResponseEntity<?> getByBenhNhan(@PathVariable Integer maBenhNhan) {

        if (biChanXemDuLieuBenhNhan(maBenhNhan)) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Bạn không có quyền xem lịch hẹn của bệnh nhân khác!"));
        }

        return ResponseEntity.ok(service.getByBenhNhan(maBenhNhan));
    }

    @GetMapping("/benh-nhan/{maBenhNhan}/lich-su")
    public ResponseEntity<?> getLichSuKham(@PathVariable Integer maBenhNhan) {

        if (biChanXemDuLieuBenhNhan(maBenhNhan)) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Bạn không có quyền xem lịch sử khám của bệnh nhân khác!"));
        }

        return ResponseEntity.ok(service.getLichSuKham(maBenhNhan));
    }

    /**
     * Bác sĩ / tiếp tân / admin xem được của mọi bệnh nhân;
     * bệnh nhân chỉ xem được dữ liệu của chính mình.
     */
    private boolean biChanXemDuLieuBenhNhan(Integer maBenhNhan) {
        return !nguoiDung.laNhanVien() && !nguoiDung.laChuSoHuuBenhNhan(maBenhNhan);
    }

    @GetMapping("/lich-lam-viec/{maLich}")
    public List<LichHen> getByLich(@PathVariable Integer maLich) {
        return service.getByLich(maLich);
    }

    @GetMapping("/trang-thai/{trangThai}")
    public List<LichHen> getByTrangThai(@PathVariable String trangThai) {
        return service.getByTrangThai(trangThai);
    }
    
    @PostMapping("/dat-lich")
    public ResponseEntity<?> datLichKham(@Valid @RequestBody DatLichHenRequest request) {
        // Lấy mã bệnh nhân từ tài khoản đang đăng nhập, không tin giá trị client gửi lên
    Integer maBn = nguoiDung.maBenhNhan().orElse(null);

    if (maBn == null) {
        return ResponseEntity.status(403)
                .body(Map.of("message", "Không tìm thấy hồ sơ bệnh nhân!"));
    }

    request.setMaBenhNhan(maBn);

        try {
            return ResponseEntity.ok(service.datLichKham(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Chỉ Tiếp tân / Admin (SecurityConfig đã giới hạn quyền)
    @PatchMapping("/{id}/duyet")
    public ResponseEntity<?> duyetLichHen(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(service.duyetLichHen(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/huy")
    public ResponseEntity<?> huyLichHen(
            @PathVariable Integer id,
            @RequestParam(required = false) String lyDo) {
        try {
            return ResponseEntity.ok(service.huyLichHen(id, lyDo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/trang-thai")
    public ResponseEntity<?> updateTrangThai(
            @PathVariable Integer id,
            @RequestParam String trangThai) {
        try {
            return ResponseEntity.ok(service.capNhatTrangThai(id, trangThai));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
public ResponseEntity<?> delete(@PathVariable Integer id) {
    Optional<LichHen> opt = service.getById(id);

    if (opt.isEmpty()) {
        return ResponseEntity.notFound().build();
    }

    // Bệnh nhân chỉ được xóa lịch của chính mình
    if (!nguoiDung.laNhanVien()
            && !nguoiDung.laChuSoHuuBenhNhan(opt.get().getMaBenhNhan())) {
        return ResponseEntity.status(403)
                .body(Map.of("message", "Bạn không có quyền xóa lịch hẹn của người khác!"));
    }

    try {
        service.xoaLichHen(id, nguoiDung.laNhanVien());
        return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
}
}