package quanly.phongkham.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Giới hạn số lần gọi /api/tu-van-ai/phan-tich theo địa chỉ IP,
 * vì endpoint này công khai và mỗi lần gọi tốn chi phí API.
 * Bộ đếm lưu trong bộ nhớ: đủ cho một máy chủ; nhiều máy chủ thì dùng Redis/Bucket4j.
 */
@Component
public class TuVanAIRateLimitFilter extends OncePerRequestFilter {

    private static final int TOI_DA = 10;            // số lần cho phép
    private static final long CUA_SO_MS = 60_000L;   // trong 1 phút

    private record Dem(long batDau, int soLan) {}

    private final ConcurrentHashMap<String, Dem> bang = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equalsIgnoreCase(request.getMethod())
                && "/api/tu-van-ai/phan-tich".equals(request.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        long now = System.currentTimeMillis();
        String ip = request.getRemoteAddr();

        Dem dem = bang.merge(ip, new Dem(now, 1), (cu, moi) ->
                now - cu.batDau() > CUA_SO_MS ? moi : new Dem(cu.batDau(), cu.soLan() + 1));

        if (bang.size() > 10_000) { // dọn bớt để không phình bộ nhớ
            bang.entrySet().removeIf(e -> now - e.getValue().batDau() > CUA_SO_MS);
        }

        if (dem.soLan() > TOI_DA) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(
                    "{\"message\":\"Bạn gửi quá nhanh. Vui lòng đợi một phút rồi thử lại.\"}");
            return;
        }

        chain.doFilter(request, response);
    }
}