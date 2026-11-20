package quanly.phongkham.dto;

import java.time.LocalDateTime;

public final class TuVanAIDto {

    private TuVanAIDto() {}

    /** Body frontend gửi lên: { "noiDungTrieuChung": "..." } */
    public record PhanTichRequest(String noiDungTrieuChung) {}

    /** Body trả về cho widget chat. */
    public record PhanTichResponse(
            String nhanDinhAI,
            Integer maChuyenKhoaGoiY,
            String tenChuyenKhoaGoiY
    ) {}

    /** Một dòng lịch sử tư vấn của bệnh nhân. */
    public record LichSuItem(
            Integer maTuVan,
            String noiDungTrieuChung,
            String nhanDinhAI,
            Integer maChuyenKhoaGoiY,
            LocalDateTime thoiGianTuVan
    ) {}
}