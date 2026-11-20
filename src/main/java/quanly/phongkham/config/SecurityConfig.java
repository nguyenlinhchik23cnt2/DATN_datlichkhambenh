package quanly.phongkham.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import quanly.phongkham.entity.TaiKhoan;
import quanly.phongkham.repository.TaiKhoanRepository;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class SecurityConfig {

    private final TaiKhoanRepository taiKhoanRepository;

    public SecurityConfig(TaiKhoanRepository taiKhoanRepository) {
        this.taiKhoanRepository = taiKhoanRepository;
    }

    // ================= PASSWORD ENCODER =================

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ================= SECURITY CONTEXT =================

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    // ================= USER DETAILS SERVICE =================

    @Bean
    public UserDetailsService userDetailsService() {

        return tenDangNhap -> {

            TaiKhoan tk = taiKhoanRepository
                    .findByTenDangNhap(tenDangNhap)
                    .orElseThrow(() ->
                            new UsernameNotFoundException(
                                    "Không tìm thấy tài khoản: " + tenDangNhap
                            )
                    );

            if (Boolean.FALSE.equals(tk.getTrangThai())) {
                throw new UsernameNotFoundException(
                        "Tài khoản đã bị khóa: " + tenDangNhap
                );
            }

            String vaiTro = tk.getVaiTro() == null
                    ? ""
                    : tk.getVaiTro().trim();

            return User.withUsername(tk.getTenDangNhap())
                    .password(tk.getMatKhau())
                    .authorities(vaiTro)
                    .build();
        };
    }

    // ================= AUTHENTICATION MANAGER =================

    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(provider);
    }

    // ================= CORS CONFIGURATION =================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173"
        ));

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "PATCH",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    // ================= SECURITY FILTER CHAIN =================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http

            // Không sử dụng CSRF trong cấu hình hiện tại
            .csrf(csrf -> csrf.disable())

            // CORS cho ReactJS
            .cors(cors -> cors.configurationSource(
                    corsConfigurationSource()
            ))

            // Sử dụng session đăng nhập
            .sessionManagement(session -> session
                    .sessionCreationPolicy(
                            SessionCreationPolicy.IF_REQUIRED
                    )
            )

            .securityContext(context -> context
                    .securityContextRepository(
                            securityContextRepository()
                    )
            )

            // Xử lý lỗi đăng nhập và phân quyền
            .exceptionHandling(handling -> handling

                    .authenticationEntryPoint((request, response, ex) -> {

                        response.setStatus(401);
                        response.setContentType(
                                MediaType.APPLICATION_JSON_VALUE
                        );
                        response.setCharacterEncoding(
                                StandardCharsets.UTF_8.name()
                        );

                        response.getWriter().write(
                                "{\"message\":\"Vui lòng đăng nhập để thực hiện thao tác này!\"}"
                        );
                    })

                    .accessDeniedHandler((request, response, ex) -> {

                        response.setStatus(403);
                        response.setContentType(
                                MediaType.APPLICATION_JSON_VALUE
                        );
                        response.setCharacterEncoding(
                                StandardCharsets.UTF_8.name()
                        );

                        response.getWriter().write(
                                "{\"message\":\"Bạn không có quyền thực hiện thao tác này!\"}"
                        );
                    })
            )

            // Tắt trang đăng nhập mặc định của Spring Security
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .logout(logout -> logout.disable())

            // ================= PHÂN QUYỀN API =================

            .authorizeHttpRequests(auth -> auth

                    // -----------------------------------------
                    // 1. ĐĂNG NHẬP / ĐĂNG KÝ / ĐĂNG XUẤT
                    // -----------------------------------------

                    // Đăng xuất luôn thành công, kể cả khi phiên đã hết hạn
                    .requestMatchers(HttpMethod.POST,
                            "/api/tai-khoan/dang-nhap",
                            "/api/tai-khoan/login",
                            "/api/tai-khoan/dang-ky",
                            "/api/tai-khoan/dang-ky-benh-nhan",
                            "/api/tai-khoan/dang-xuat",
                            "/api/tai-khoan",
                            "/api/benh-nhan/dang-ky"
                    ).permitAll()

                    // Lấy thông tin tài khoản của phiên hiện tại (mọi vai trò).
                    // PHẢI đặt trước rule GET "/api/tai-khoan/*" của Admin ở mục 11.
                    // Chưa đăng nhập thì trả 401 qua authenticationEntryPoint.
                    .requestMatchers(HttpMethod.GET,
                            "/api/tai-khoan/me"
                    ).authenticated()


                    // -----------------------------------------
                    // 2. CHUYÊN KHOA CÔNG KHAI
                    // -----------------------------------------

                    .requestMatchers(HttpMethod.GET,
                            "/api/chuyen-khoa",
                            "/api/chuyen-khoa/dang-hoat-dong",
                            "/api/chuyen-khoa/tim-kiem",
                            "/api/chuyen-khoa/*"
                    ).permitAll()


                    // -----------------------------------------
                    // 3. BÁC SĨ CÔNG KHAI
                    // -----------------------------------------

                    .requestMatchers(HttpMethod.GET,
                            "/api/bac-si",
                            "/api/bac-si/dang-hoat-dong",
                            "/api/bac-si/danh-sach-chi-tiet",
                            "/api/bac-si/*",
                            "/api/bac-si/chuyen-khoa/*",
                            "/api/bac-si/chuyen-khoa/*/dang-hoat-dong"
                    ).permitAll()


                    // -----------------------------------------
                    // 4. LỊCH LÀM VIỆC
                    // -----------------------------------------

                    // Mọi người được xem lịch làm việc
                    .requestMatchers(HttpMethod.GET,
                            "/api/lich-lam-viec/**"
                    ).permitAll()

                    // Bác sĩ, Tiếp tân, Admin quản lý lịch làm việc
                    .requestMatchers(HttpMethod.POST,
                            "/api/lich-lam-viec"
                    ).hasAnyAuthority(
                            "BacSi", "TiepTan", "Admin"
                    )

                    .requestMatchers(HttpMethod.PUT,
                            "/api/lich-lam-viec/*"
                    ).hasAnyAuthority(
                            "BacSi", "TiepTan", "Admin"
                    )

                    .requestMatchers(HttpMethod.DELETE,
                            "/api/lich-lam-viec/*"
                    ).hasAnyAuthority(
                            "BacSi", "TiepTan", "Admin"
                    )


                    // -----------------------------------------
                    // 5. LỊCH HẸN - BÁC SĨ
                    // -----------------------------------------

                    // Bác sĩ chỉ xem lịch của mình
                    .requestMatchers(HttpMethod.GET,
                            "/api/lich-hen/bac-si-cua-toi"
                    ).hasAuthority("BacSi")


                    // -----------------------------------------
                    // 6. LỊCH HẸN - TRA CỨU
                    // -----------------------------------------

                    // Tra cứu bằng mã đặt lịch
                    .requestMatchers(HttpMethod.GET,
                            "/api/lich-hen/ma-dat-lich/*"
                    ).permitAll()

                    // Bệnh nhân, Tiếp tân, Admin xem lịch theo bệnh nhân
                    .requestMatchers(HttpMethod.GET,
                            "/api/lich-hen/benh-nhan/**"
                    ).hasAnyAuthority(
                            "BenhNhan", "TiepTan", "Admin"
                    )

                                       // Xóa lịch hẹn: bệnh nhân (chỉ lịch quá hạn của chính mình),
                    // tiếp tân, admin. Quyền sở hữu và điều kiện xóa được kiểm tra
                    // trong LichHenController / LichHenService
                    .requestMatchers(HttpMethod.DELETE,
                            "/api/lich-hen/*"
                    ).hasAnyAuthority(
                            "BenhNhan", "TiepTan", "Admin"
                    )

                    // Tiếp tân và Admin xem toàn bộ lịch

                    // Các API GET lịch hẹn còn lại
                    // Chỉ Tiếp tân và Admin được sử dụng
                    .requestMatchers(HttpMethod.GET,
                            "/api/lich-hen/**"
                    ).hasAnyAuthority(
                            "TiepTan", "Admin"
                    )


                    // -----------------------------------------
                    // 7. LỊCH HẸN - BỆNH NHÂN ĐẶT LỊCH
                    // -----------------------------------------

                    .requestMatchers(HttpMethod.POST,
                            "/api/lich-hen/dat-lich"
                    ).hasAuthority("BenhNhan")


                    // -----------------------------------------
                    // 8. LỊCH HẸN - TIẾP TÂN DUYỆT / HỦY
                    // -----------------------------------------

                    .requestMatchers(HttpMethod.PATCH,
                            "/api/lich-hen/*/duyet",
                            "/api/lich-hen/*/huy"
                    ).hasAnyAuthority(
                            "TiepTan", "Admin"
                    )


                    // -----------------------------------------
                    // 9. LỊCH HẸN - ADMIN QUẢN LÝ
                    // -----------------------------------------

                    .requestMatchers(HttpMethod.PATCH,
                            "/api/lich-hen/*/trang-thai"
                    ).hasAuthority("Admin")

                    .requestMatchers(HttpMethod.POST,
                            "/api/lich-hen"
                    ).hasAuthority("Admin")

                    .requestMatchers(HttpMethod.PUT,
                            "/api/lich-hen/*"
                    ).hasAuthority("Admin")

                    .requestMatchers(HttpMethod.DELETE,
                            "/api/lich-hen/*"
                    ).hasAuthority("Admin")


                    // -----------------------------------------
                    // 10. KHÁM BỆNH
                    // -----------------------------------------

                    // Bác sĩ và Admin được lưu kết quả khám
                    .requestMatchers(HttpMethod.POST,
                            "/api/kham-benh/hoan-thanh"
                    ).hasAnyAuthority(
                            "BacSi", "Admin"
                    )


                    // -----------------------------------------
                    // 11. QUẢN TRỊ HỆ THỐNG
                    // -----------------------------------------

                    .requestMatchers("/api/thong-ke/**")
                            .hasAuthority("Admin")

                    .requestMatchers(HttpMethod.POST,
                            "/api/bac-si/tao-moi"
                    ).hasAuthority("Admin")

                    .requestMatchers(HttpMethod.PUT,
                            "/api/chuyen-khoa/*"
                    ).hasAuthority("Admin")

                    .requestMatchers(HttpMethod.POST,
                            "/api/chuyen-khoa"
                    ).hasAuthority("Admin")

                    .requestMatchers(HttpMethod.DELETE,
                            "/api/chuyen-khoa/*"
                    ).hasAuthority("Admin")

                    .requestMatchers(HttpMethod.DELETE,
                            "/api/bac-si/*"
                    ).hasAuthority("Admin")

                    .requestMatchers(HttpMethod.DELETE,
                            "/api/tai-khoan/*"
                    ).hasAuthority("Admin")

                    .requestMatchers(HttpMethod.GET,
                            "/api/tai-khoan",
                            "/api/tai-khoan/*"
                    ).hasAuthority("Admin")

                    .requestMatchers(HttpMethod.PUT,
                            "/api/tai-khoan/*"
                    ).hasAuthority("Admin")


                    // -----------------------------------------
                    // 12. KIỂM TRA DATABASE
                    // -----------------------------------------

                    .requestMatchers("/api/test-db")
                            .hasAuthority("Admin")

                    // -----------------------------------------
                    // -----------------------------------------
                    // 12b. TƯ VẤN AI
                    // -----------------------------------------

                    .requestMatchers(HttpMethod.POST,
                            "/api/tu-van-ai/phan-tich"
                    ).permitAll()

                    .requestMatchers(HttpMethod.GET,
                            "/api/tu-van-ai/cua-toi"
                    ).hasAuthority("BenhNhan")

                    .requestMatchers(
                            "/api/tu-van-ai",
                            "/api/tu-van-ai/**"
                    ).hasAuthority("Admin")

                    // -----------------------------------------
                    // 13. CÁC API CÒN LẠI
                    // -----------------------------------------

                    .anyRequest().authenticated()
            );

        return http.build();
        }
}