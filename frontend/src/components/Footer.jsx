import React from 'react';
import { Activity, Phone, Mail, MapPin } from 'lucide-react';

export default function Footer() {
  return (
    <footer className="bg-slate-900 text-slate-400 text-sm mt-auto border-t border-slate-800">
      <div className="max-w-7xl mx-auto px-4 py-10 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div>
            <div className="flex items-center gap-2 text-white font-bold text-lg mb-3">
              <div className="p-1.5 bg-cyan-600 rounded-lg">
                <Activity className="w-5 h-5 text-white" />
              </div>
              Phòng Khám Đa Khoa
            </div>
            <p className="text-slate-400 text-xs leading-relaxed">
              Hệ thống phòng khám đa khoa chuyên nghiệp, hiện đại. Cung cấp dịch vụ chăm sóc sức khỏe toàn diện với công nghệ hỗ trợ tư vấn thông minh.
            </p>
          </div>
          <div>
            <h4 className="text-white font-semibold text-sm mb-3">Thông Tin Liên Hệ</h4>
            <ul className="space-y-2 text-xs">
              <li className="flex items-center gap-2">
                <MapPin className="w-4 h-4 text-cyan-400 shrink-0" />
                <span>Số 123 Đường Sức Khỏe, Quận Cầu Giấy, Hà Nội</span>
              </li>
              <li className="flex items-center gap-2">
                <Phone className="w-4 h-4 text-cyan-400 shrink-0" />
                <span>Hotline: 1900 6868 - (024) 3888 9999</span>
              </li>
              <li className="flex items-center gap-2">
                <Mail className="w-4 h-4 text-cyan-400 shrink-0" />
                <span>hotro@phongkhamdakhoa.vn</span>
              </li>
            </ul>
          </div>
          <div>
            <h4 className="text-white font-semibold text-sm mb-3">Giờ Làm Việc</h4>
            <p className="text-xs text-slate-400 leading-relaxed mb-2">
              Thứ Hai - Thứ Bảy: 07:30 - 17:30<br />
              Chủ Nhật: 08:00 - 12:00
            </p>
            <span className="inline-block px-2.5 py-1 text-[11px] font-medium bg-emerald-950 text-emerald-400 border border-emerald-800 rounded-full">
              ● Tiếp nhận cấp cứu & tư vấn 24/7
            </span>
          </div>
        </div>
        <div className="border-t border-slate-800 mt-8 pt-6 text-center text-xs text-slate-500">
          © 2026 Phòng Khám Đa Khoa. Bản quyền thuộc về Hệ thống Quản Lý Phòng Khám (DATN K23CNT2).
        </div>
      </div>
    </footer>
  );
}

