package quanly.phongkham.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Gom loi ve mot dang JSON thong nhat, khong lo stack trace ra ngoai.
 */
@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    /** Loi validate du lieu dau vao (400). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> chiTiet = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            chiTiet.putIfAbsent(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Dữ liệu không hợp lệ!");
        body.put("errors", chiTiet);
        return ResponseEntity.badRequest().body(body);
    }

    /** Loi nghiep vu nem ra tu service (400). */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", String.valueOf(ex.getMessage())));
    }

    /** Vi pham rang buoc CSDL: trung khoa, vi pham CHECK/FK (409). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String goc = ex.getMostSpecificCause().getMessage();
        String thongBao = "Dữ liệu vi phạm ràng buộc của hệ thống!";

        if (goc != null) {
            String thuong = goc.toLowerCase();
            if (thuong.contains("unique") || thuong.contains("duplicate")) {
                thongBao = "Dữ liệu đã tồn tại, vui lòng kiểm tra lại!";
            } else if (thuong.contains("cannot insert the value null")) {
                thongBao = "Thiếu thông tin bắt buộc!";
            } else if (thuong.contains("check constraint")) {
                thongBao = "Dữ liệu không thỏa điều kiện của hệ thống!";
            } else if (thuong.contains("reference constraint") || thuong.contains("foreign key")) {
                thongBao = "Dữ liệu đang được sử dụng ở nơi khác, không thể thay đổi!";
            }
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", thongBao));
    }

    /** Giữ nguyên mã lỗi do controller chủ động ném ra (429, 403, 404...). */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleStatus(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(Map.of("message", ex.getReason() != null ? ex.getReason() : "Yêu cầu không hợp lệ!"));
    }

    /** JSON gửi lên sai định dạng hoặc thiếu body (400). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", "Dữ liệu gửi lên không đúng định dạng!"));
    }

    /** Loi he thong con lai (500) - ghi log de debug, khong tra stack trace. */
   @ExceptionHandler(Exception.class)
public ResponseEntity<Map<String, Object>> handleOthers(Exception ex) {
    // Lỗi Spring đã có sẵn mã (404, 405, 415...): giữ nguyên mã đó
    if (ex instanceof org.springframework.web.ErrorResponse er) {
        return ResponseEntity.status(er.getStatusCode())
                .body(Map.of("message", "Yêu cầu không hợp lệ!"));
    }
    log.error("Lỗi hệ thống không xử lý được", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("message", "Đã xảy ra lỗi hệ thống, vui lòng thử lại sau!"));
}
}