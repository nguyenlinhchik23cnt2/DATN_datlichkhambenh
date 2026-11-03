package quanly.phongkham;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DatabaseController {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/api/test-db")
    public String testDatabase() {

        String database = jdbcTemplate.queryForObject(
            "SELECT DB_NAME()",
            String.class
        );

        return "Ket noi thanh cong! Database: " + database;
    }
}