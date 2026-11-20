import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Calendar, Stethoscope, ArrowRight, ShieldCheck } from 'lucide-react';
import api from '../api/client';

// Container chung cho các section để căn lề đồng nhất
const SECTION_CLASS = 'max-w-7xl mx-auto px-4 sm:px-6 lg:px-8';

// Tiêu đề section dùng lại cho nhiều khối
function SectionHeading({ title, description }) {
  return (
    <div className="text-center max-w-2xl mx-auto mb-10">
      <h2 className="text-2xl sm:text-3xl font-bold text-slate-900">{title}</h2>
      <p className="text-slate-500 text-sm mt-2">{description}</p>
    </div>
  );
}

export default function HomePage() {
  const [chuyenKhoas, setChuyenKhoas] = useState([]);
  const [bacSis, setBacSis] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [ckRes, bsRes] = await Promise.all([
          api.get('/chuyen-khoa/dang-hoat-dong'),
          api.get('/bac-si/danh-sach-chi-tiet')
        ]);
        setChuyenKhoas(ckRes.data || []);
        setBacSis(bsRes.data || []);
      } catch (err) {
        console.error('Lỗi khi tải dữ liệu trang chủ:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  return (
    <div className="space-y-16 pb-16">

      {/* Hero Section */}
      <section className="relative overflow-hidden bg-gradient-to-br from-sky-500 via-sky-600 to-sky-800 text-white py-16 lg:py-24">
        <div className={`${SECTION_CLASS} relative z-10`}>
          <div className="max-w-3xl space-y-6">

            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-white/15 backdrop-blur text-sky-50 border border-white/20">
              <ShieldCheck className="w-4 h-4 text-sky-200" />
              Chuẩn đoán chính xác • Đặt lịch nhanh chóng
            </span>

            <h1 className="text-3xl sm:text-4xl lg:text-5xl font-extrabold tracking-tight leading-tight">
              Chăm Sóc Sức Khỏe Toàn Diện Cho Bạn & Gia Đình
            </h1>

            <p className="text-base sm:text-lg text-sky-50/90 leading-relaxed">
              Đặt lịch khám theo khung giờ 30 phút tiện lợi với đội ngũ bác sĩ chuyên khoa giàu kinh nghiệm.
            </p>

            <div className="flex flex-col sm:flex-row sm:flex-wrap gap-3 sm:gap-4 pt-2">
              <Link
                to="/dat-lich"
                className="inline-flex items-center justify-center gap-2 px-6 py-3.5 rounded-xl font-bold bg-black text-white hover:bg-slate-800 shadow-lg shadow-black/20 transition"
              >
                <Calendar className="w-5 h-5" />
                Đặt Lịch Khám Ngay
              </Link>
            </div>

          </div>
        </div>
      </section>

      {/* Chuyên Khoa Section */}
      <section className={SECTION_CLASS}>
        <SectionHeading
          title="Các Chuyên Khoa Nổi Bật"
          description="Đa dạng chuyên khoa y tế với trang thiết bị hiện đại và phác đồ điều trị chuẩn y khoa"
        />

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {loading ? (
            Array.from({ length: 4 }).map((_, i) => (
              <div key={i} className="h-44 bg-slate-100 rounded-2xl animate-pulse" />
            ))
          ) : chuyenKhoas.length > 0 ? (
            chuyenKhoas.map((ck) => (
              <article
                key={ck.maChuyenKhoa}
                className="group flex h-full flex-col p-6 bg-white rounded-2xl border border-slate-200 shadow-sm hover:shadow-md hover:border-sky-400 transition"
              >
                <div className="w-12 h-12 shrink-0 rounded-xl bg-sky-50 text-sky-600 flex items-center justify-center mb-4 group-hover:bg-sky-500 group-hover:text-white transition">
                  <Stethoscope className="w-6 h-6" />
                </div>

                <h3 className="font-bold text-slate-900 text-base mb-1.5 line-clamp-2">
                  {ck.tenChuyenKhoa}
                </h3>

                <p className="text-slate-500 text-xs leading-relaxed line-clamp-3">
                  {ck.moTa || 'Chuyên khoa khám và điều trị chuyên sâu.'}
                </p>

                {/* mt-auto đẩy link xuống đáy để các thẻ thẳng hàng */}
                <Link
                  to={`/dat-lich?chuyenKhoa=${ck.maChuyenKhoa}`}
                  className="mt-auto pt-4 inline-flex items-center gap-1 text-xs font-semibold text-black hover:text-slate-600"
                >
                  Đặt lịch khám <ArrowRight className="w-3.5 h-3.5" />
                </Link>
              </article>
            ))
          ) : (
            <div className="col-span-full text-center py-8 text-slate-400 text-sm">
              Chưa có chuyên khoa nào
            </div>
          )}
        </div>
      </section>

      {/* Đội Ngũ Bác Sĩ Section */}
      <section className={SECTION_CLASS}>
        <SectionHeading
          title="Đội Ngũ Bác Sĩ Chuyên Môn"
          description="Bác sĩ giàu kinh nghiệm, tận tâm vì sức khỏe người bệnh"
        />

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {loading ? (
            Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="h-56 bg-slate-100 rounded-2xl animate-pulse" />
            ))
          ) : bacSis.length > 0 ? (
            bacSis.slice(0, 6).map((bs) => (
              <article
                key={bs.maBacSi}
                className="flex h-full flex-col p-6 bg-white rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition"
              >
                <div className="flex items-center gap-4 mb-4">
                  <div className="w-14 h-14 shrink-0 rounded-2xl bg-sky-100 text-sky-700 font-extrabold text-xl flex items-center justify-center">
                    {bs.tenBacSi?.charAt(0) || 'B'}
                  </div>

                  {/* min-w-0 để tên dài tự cắt, không làm vỡ thẻ */}
                  <div className="min-w-0">
                    <span className="block text-xs font-semibold text-sky-600 uppercase tracking-wide truncate">
                      {bs.hocVi || 'Bác sĩ chuyên khoa'}
                    </span>
                    <h3 className="font-bold text-slate-900 text-base truncate">
                      {bs.tenBacSi}
                    </h3>
                    <span className="block text-xs text-slate-500 font-medium truncate">
                      {bs.tenChuyenKhoa}
                    </span>
                  </div>
                </div>

                <p className="text-slate-600 text-xs line-clamp-3 leading-relaxed mb-4">
                  {bs.gioiThieu || bs.kinhNghiem || 'Nhiều năm kinh nghiệm công tác tại các bệnh viện tuyến đầu.'}
                </p>

                {/* block + mt-auto: nút full chiều rộng và luôn nằm đáy thẻ */}
                <Link
                  to={`/dat-lich?bacSi=${bs.maBacSi}`}
                  className="mt-auto block w-full py-2.5 rounded-xl text-center text-xs font-bold text-white bg-black hover:bg-slate-800 transition"
                >
                  Chọn Bác Sĩ & Đặt Giờ Khám
                </Link>
              </article>
            ))
          ) : (
            <div className="col-span-full text-center py-8 text-slate-400 text-sm">
              Chưa có thông tin bác sĩ
            </div>
          )}
        </div>
      </section>

    </div>
  );
}