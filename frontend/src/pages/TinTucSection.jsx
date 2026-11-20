import React, { useEffect, useState } from 'react';
import api from '../api/client';

const formatNgay = (value) => {
  if (!value) return '';
  const d = new Date(value);
  return Number.isNaN(d.getTime()) ? '' : d.toLocaleDateString('vi-VN');
};

// Chỉ cho phép mở link http/https
const laLinkHopLe = (url) => /^https?:\/\//i.test(url || '');

// Ảnh bìa; nếu không có ảnh hoặc ảnh lỗi thì hiện nền pastel trơn (không icon)
function AnhBia({ src, className }) {
  const [loi, setLoi] = useState(false);

  if (!src || loi) {
    return <div className={`bg-gradient-to-br from-sky-100 to-emerald-100 ${className}`} />;
  }

  return (
    <img
      src={src}
      alt=""
      loading="lazy"
      onError={() => setLoi(true)}
      className={`object-cover ${className}`}
    />
  );
}

export default function TinTucSection() {
  const [baiViets, setBaiViets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState(null);

  useEffect(() => {
    api.get('/bai-viet', { params: { limit: 6 } })
      .then((res) => setBaiViets(Array.isArray(res.data) ? res.data : []))
      .catch((err) => console.error('Lỗi tải bài viết:', err))
      .finally(() => setLoading(false));
  }, []);

  // Đóng cửa sổ chi tiết bằng phím Esc
  useEffect(() => {
    if (!selected) return;
    const onKey = (e) => e.key === 'Escape' && setSelected(null);
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [selected]);

  // Không có bài nào thì ẩn hẳn mục này khỏi trang chủ
  if (!loading && baiViets.length === 0) return null;

  return (
    <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
      <div className="max-w-2xl mb-10">
        <h2 className="text-2xl sm:text-3xl font-bold text-slate-800">Tin tức và hoạt động</h2>
        <p className="text-slate-500 text-sm mt-2 leading-relaxed">
          Thông tin mới về phòng khám và sức khỏe.
        </p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
        {loading
          ? Array(3).fill(0).map((_, i) => (
              <div key={i} className="h-72 bg-white/60 rounded-2xl animate-pulse"></div>
            ))
          : baiViets.map((b) => (
              <button
                key={b.maBaiViet}
                type="button"
                onClick={() => setSelected(b)}
                className="text-left bg-white rounded-2xl border border-sky-100 hover:border-teal-300 transition overflow-hidden flex flex-col focus:outline-none focus-visible:ring-2 focus-visible:ring-teal-500"
              >
                <AnhBia src={b.anhBia} className="w-full h-44" />
                <div className="p-5 flex-1 flex flex-col">
                  <p className="text-xs text-slate-500 mb-2">
                    {formatNgay(b.ngayDang)}
                    {b.nguonBaiViet ? `, ${b.nguonBaiViet}` : ''}
                  </p>
                  <h3 className="font-bold text-slate-800 text-base line-clamp-2 mb-2">{b.tieuDe}</h3>
                  <p className="text-slate-600 text-xs line-clamp-3 leading-relaxed">
                    {b.tomTat || 'Bấm để xem chi tiết bài viết.'}
                  </p>
                </div>
              </button>
            ))}
      </div>

      {/* Cửa sổ xem chi tiết */}
      {selected && (
        <div
          className="fixed inset-0 z-[60] bg-slate-900/40 flex items-center justify-center p-4"
          onClick={() => setSelected(null)}
          role="dialog"
          aria-modal="true"
        >
          <div
            className="bg-white rounded-3xl shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto"
            onClick={(e) => e.stopPropagation()}
          >
            <AnhBia src={selected.anhBia} className="w-full h-56 rounded-t-3xl" />

            <div className="p-6 sm:p-8 space-y-4">
              <p className="text-xs text-slate-500">
                {formatNgay(selected.ngayDang)}
                {selected.nguonBaiViet ? `, nguồn: ${selected.nguonBaiViet}` : ''}
              </p>

              <h3 className="text-xl sm:text-2xl font-extrabold text-slate-800">{selected.tieuDe}</h3>

              {selected.tomTat && (
                <p className="text-sm text-slate-600 font-medium leading-relaxed">{selected.tomTat}</p>
              )}

              {selected.noiDung && (
                <p className="text-sm text-slate-700 leading-relaxed whitespace-pre-line">{selected.noiDung}</p>
              )}

              <div className="flex flex-wrap gap-3 pt-2">
                {laLinkHopLe(selected.duongDanNguon) && (
                  <a
                    href={selected.duongDanNguon}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="px-4 py-2.5 rounded-xl bg-teal-200 border border-teal-300 hover:bg-teal-300 text-teal-900 text-xs font-bold transition"
                  >
                    Đọc bài gốc
                  </a>
                )}
                <button
                  type="button"
                  onClick={() => setSelected(null)}
                  className="px-4 py-2.5 rounded-xl bg-sky-100 border border-sky-200 hover:bg-sky-200 text-sky-900 text-xs font-bold transition"
                >
                  Đóng
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </section>
  );
}