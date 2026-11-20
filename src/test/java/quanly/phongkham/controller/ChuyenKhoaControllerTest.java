package quanly.phongkham.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import quanly.phongkham.entity.ChuyenKhoa;
import quanly.phongkham.repository.ChuyenKhoaRepository;
import quanly.phongkham.service.ChuyenKhoaService;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ChuyenKhoaControllerTest {

    private MockMvc mockMvc;
    private final Map<Integer, ChuyenKhoa> mockDb = new HashMap<>();
    private int idSeq = 1;

    @BeforeEach
    void setUp() {
        mockDb.clear();
        idSeq = 1;

        ChuyenKhoaRepository stubRepo = (ChuyenKhoaRepository) Proxy.newProxyInstance(
                ChuyenKhoaRepository.class.getClassLoader(),
                new Class<?>[]{ChuyenKhoaRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("save".equals(name)) {
                        ChuyenKhoa ck = (ChuyenKhoa) args[0];
                        if (ck.getMaChuyenKhoa() == null) {
                            ck.setMaChuyenKhoa(idSeq++);
                        }
                        mockDb.put(ck.getMaChuyenKhoa(), ck);
                        return ck;
                    }
                    if ("findAll".equals(name)) {
                        return new ArrayList<>(mockDb.values());
                    }
                    if ("findByTrangThaiTrue".equals(name)) {
                        return mockDb.values().stream()
                                .filter(c -> Boolean.TRUE.equals(c.getTrangThai()))
                                .toList();
                    }
                    if ("findById".equals(name)) {
                        return Optional.ofNullable(mockDb.get((Integer) args[0]));
                    }
                    if ("findByTenChuyenKhoaContainingIgnoreCase".equals(name)) {
                        String keyword = ((String) args[0]).toLowerCase();
                        return mockDb.values().stream()
                                .filter(c -> c.getTenChuyenKhoa() != null && c.getTenChuyenKhoa().toLowerCase().contains(keyword))
                                .toList();
                    }
                    if ("deleteById".equals(name)) {
                        mockDb.remove((Integer) args[0]);
                        return null;
                    }
                    return null;
                }
        );

        ChuyenKhoaService service = new ChuyenKhoaService(stubRepo);
        ChuyenKhoaController controller = new ChuyenKhoaController(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("CRUD - READ ALL: GET /api/chuyen-khoa")
    void testGetAll() throws Exception {
        mockDb.put(1, new ChuyenKhoa(1, "Nhi Khoa", "Khám bệnh cho trẻ em", true));
        mockDb.put(2, new ChuyenKhoa(2, "Tai Mũi Họng", "Khám TMH", true));

        mockMvc.perform(get("/api/chuyen-khoa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].tenChuyenKhoa", is("Nhi Khoa")));
    }

    @Test
    @DisplayName("CRUD - READ ACTIVE: GET /api/chuyen-khoa/dang-hoat-dong")
    void testGetActive() throws Exception {
        mockDb.put(1, new ChuyenKhoa(1, "Nhi Khoa", "Mô tả 1", true));
        mockDb.put(2, new ChuyenKhoa(2, "Khoa Tạm Dừng", "Mô tả 2", false));

        mockMvc.perform(get("/api/chuyen-khoa/dang-hoat-dong"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tenChuyenKhoa", is("Nhi Khoa")));
    }

    @Test
    @DisplayName("CRUD - SEARCH: GET /api/chuyen-khoa/tim-kiem?ten=tim")
    void testSearch() throws Exception {
        mockDb.put(1, new ChuyenKhoa(1, "Khoa Tim Mạch", "Chuyên tim mạch", true));
        mockDb.put(2, new ChuyenKhoa(2, "Khoa Da Liễu", "Chuyên da liễu", true));

        mockMvc.perform(get("/api/chuyen-khoa/tim-kiem?ten=tim"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tenChuyenKhoa", is("Khoa Tim Mạch")));
    }

    @Test
    @DisplayName("CRUD - CREATE: POST /api/chuyen-khoa")
    void testCreate() throws Exception {
        String json = """
                {
                    "tenChuyenKhoa": "Răng Hàm Mặt",
                    "moTa": "Khám và điều trị răng",
                    "trangThai": true
                }
                """;

        mockMvc.perform(post("/api/chuyen-khoa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maChuyenKhoa", is(1)))
                .andExpect(jsonPath("$.tenChuyenKhoa", is("Răng Hàm Mặt")));
    }

    @Test
    @DisplayName("CRUD - UPDATE: PUT /api/chuyen-khoa/{id}")
    void testUpdate() throws Exception {
        mockDb.put(1, new ChuyenKhoa(1, "Khoa Cũ", "Mô tả cũ", true));

        String updateJson = """
                {
                    "tenChuyenKhoa": "Khoa Mới Cập Nhật",
                    "moTa": "Mô tả mới",
                    "trangThai": true
                }
                """;

        mockMvc.perform(put("/api/chuyen-khoa/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenChuyenKhoa", is("Khoa Mới Cập Nhật")))
                .andExpect(jsonPath("$.moTa", is("Mô tả mới")));
    }

    @Test
    @DisplayName("CRUD - DELETE: DELETE /api/chuyen-khoa/{id}")
    void testDelete() throws Exception {
        mockDb.put(1, new ChuyenKhoa(1, "Khoa Xóa", "Mô tả", true));

        mockMvc.perform(delete("/api/chuyen-khoa/1"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/chuyen-khoa/1"))
                .andExpect(status().isNotFound());
    }
}

