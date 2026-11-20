import { useEffect, useRef, useState } from "react";
import "./ChatbotWidget.css";

// Địa chỉ backend Spring Boot. Đổi nếu bạn dùng cổng khác hoặc proxy của Vite.
const API_BASE = import.meta.env.VITE_API_URL || "http://localhost:8080";
const ENDPOINT = `${API_BASE}/api/tu-van-ai/phan-tich`;
const GIOI_HAN_KY_TU = 2000; // khớp với length = 2000 của TuVanAI.noiDungTrieuChung

const LOI_CHAO =
  "Xin chào! Mình là trợ lý ảo của phòng khám. Bạn hãy mô tả triệu chứng đang gặp, mình sẽ gợi ý chuyên khoa phù hợp để bạn đặt lịch khám.";

const GOI_Y_NHANH = ["Đau đầu, chóng mặt vài ngày nay", "Ho kéo dài, khó thở", "Đau bụng sau khi ăn"];

function gio() {
  return new Date().toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" });
}

// Backend có thể trả về nhiều dạng; lấy phần văn bản và chuyên khoa gợi ý nếu có.
function docPhanHoi(data) {
  if (typeof data === "string") return { text: data };
  const text =
    data?.nhanDinhAI ||
    data?.nhanDinh ||
    data?.ketQua ||
    data?.traLoi ||
    data?.message ||
    "Mình chưa có nhận định cho mô tả này. Bạn thử nói rõ hơn nhé.";
  const chuyenKhoa = data?.tenChuyenKhoaGoiY || data?.chuyenKhoaGoiY?.tenChuyenKhoa || null;
  return { text, chuyenKhoa };
}

export default function ChatbotWidget() {
  const [open, setOpen] = useState(false);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [messages, setMessages] = useState([
    { id: 0, role: "ai", text: LOI_CHAO, time: gio() },
  ]);
  const cuoiRef = useRef(null);
  const oNhapRef = useRef(null);
  const idRef = useRef(1);

  // Tự cuộn xuống tin nhắn mới nhất
  useEffect(() => {
    cuoiRef.current?.scrollIntoView({ behavior: "smooth", block: "end" });
  }, [messages, loading, open]);

  // Focus vào ô nhập khi mở; Esc để đóng
  useEffect(() => {
    if (!open) return;
    oNhapRef.current?.focus();
    const onKey = (e) => e.key === "Escape" && setOpen(false);
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [open]);

  const themTinNhan = (msg) =>
    setMessages((prev) => [...prev, { id: idRef.current++, time: gio(), ...msg }]);

  async function gui(noiDung) {
    const text = (noiDung ?? input).trim();
    if (!text || loading) return;

    themTinNhan({ role: "user", text });
    setInput("");
    setLoading(true);

    try {
      const res = await fetch(ENDPOINT, {
        method: "POST",
        credentials: "include", // gửi kèm session nếu bệnh nhân đã đăng nhập
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ noiDungTrieuChung: text }),
      });

      const contentType = res.headers.get("content-type") || "";
      const data = contentType.includes("json") ? await res.json() : await res.text();

      if (!res.ok) {
        themTinNhan({
          role: "ai",
          error: true,
          text: data?.message || "Hệ thống đang bận. Bạn vui lòng thử lại sau ít phút.",
        });
      } else {
        const { text: traLoi, chuyenKhoa } = docPhanHoi(data);
        themTinNhan({ role: "ai", text: traLoi, chuyenKhoa });
      }
    } catch {
      themTinNhan({
        role: "ai",
        error: true,
        text: "Không kết nối được máy chủ. Bạn kiểm tra mạng rồi gửi lại nhé.",
      });
    } finally {
      setLoading(false);
    }
  }

  const onKeyDown = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      gui();
    }
  };

  const chiCoLoiChao = messages.length === 1;

  return (
    <div className="cb-root">
      {open && (
        <section className="cb-panel" role="dialog" aria-label="Trợ lý ảo AI">
          <header className="cb-header">
            <div className="cb-avatar" aria-hidden="true">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
                <path d="M12 5v14M5 12h14" />
              </svg>
            </div>
            <div className="cb-title">
              <strong>Trợ lý ảo AI</strong>
              <span>Gợi ý chuyên khoa theo triệu chứng</span>
            </div>
            <button className="cb-close" onClick={() => setOpen(false)} aria-label="Đóng khung chat">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
                <path d="M6 6l12 12M18 6L6 18" />
              </svg>
            </button>
          </header>

          <div className="cb-body" aria-live="polite">
            {messages.map((m) => (
              <div key={m.id} className={`cb-row cb-${m.role}`}>
                <div className={`cb-bubble${m.error ? " cb-error" : ""}`}>
                  <p>{m.text}</p>
                  {m.chuyenKhoa && (
                    <div className="cb-tag">Chuyên khoa gợi ý: {m.chuyenKhoa}</div>
                  )}
                </div>
                <time>{m.time}</time>
              </div>
            ))}

            {chiCoLoiChao && (
              <div className="cb-quick">
                {GOI_Y_NHANH.map((g) => (
                  <button key={g} onClick={() => gui(g)}>{g}</button>
                ))}
              </div>
            )}

            {loading && (
              <div className="cb-row cb-ai">
                <div className="cb-bubble cb-typing" aria-label="AI đang trả lời">
                  <span /><span /><span />
                </div>
              </div>
            )}
            <div ref={cuoiRef} />
          </div>

          <footer className="cb-footer">
            <div className="cb-note">
              Gợi ý của AI chỉ mang tính tham khảo, không thay thế chẩn đoán của bác sĩ.
            </div>
            <div className="cb-input">
              <textarea
                ref={oNhapRef}
                rows={1}
                value={input}
                maxLength={GIOI_HAN_KY_TU}
                placeholder="Mô tả triệu chứng của bạn…"
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={onKeyDown}
              />
              <button
                className="cb-send"
                onClick={() => gui()}
                disabled={!input.trim() || loading}
                aria-label="Gửi tin nhắn"
              >
                <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                  <path d="M3.4 20.4l17.4-7.5a1 1 0 000-1.8L3.4 3.6a.7.7 0 00-1 .8L4 10.5l9 1.5-9 1.5-1.6 6.1a.7.7 0 001 .8z" />
                </svg>
              </button>
            </div>
          </footer>
        </section>
      )}

      <button
        className={`cb-fab${open ? " cb-fab-open" : ""}`}
        onClick={() => setOpen((v) => !v)}
        aria-label={open ? "Đóng trợ lý ảo" : "Mở trợ lý ảo"}
        aria-expanded={open}
      >
        {open ? (
          <svg viewBox="0 0 24 24" width="26" height="26" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round">
            <path d="M6 9l6 6 6-6" />
          </svg>
        ) : (
          <svg viewBox="0 0 24 24" width="28" height="28" fill="currentColor">
            <path d="M12 3C6.9 3 3 6.6 3 11.2c0 2.4 1.1 4.6 2.9 6.1L5 21l3.9-1.9c1 .3 2 .4 3.1.4 5.1 0 9-3.6 9-8.3S17.1 3 12 3z" />
          </svg>
        )}
      </button>
    </div>
  );
}