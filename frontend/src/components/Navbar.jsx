import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Activity, Calendar, Bot, Search, LogIn, LogOut, Shield, Stethoscope, ClipboardList } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/');
  };

  return (
    <header className="sticky top-0 z-50 bg-white/95 backdrop-blur border-b border-slate-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <Link to="/" className="flex items-center gap-2">
            <div className="p-2 bg-sky-500 rounded-xl text-white shadow-sm">
              <Activity className="w-6 h-6" />
            </div>
            <div>
              <span className="text-lg font-bold text-black">Phòng Khám Đa Khoa</span>
              <span className="block text-[10px] text-black font-medium uppercase tracking-wider">Hệ thống Y tế Chất lượng cao</span>
            </div>
          </Link>

          {/* Navigation Links */}
         <nav className="hidden md:flex items-center gap-1 text-sm font-medium text-black">
  <nav className="hidden md:flex items-center gap-1 text-sm font-medium text-black">
  <Link to="/" className="flex items-center gap-1.5 px-3 py-2 rounded-lg font-semibold">
    Trang chủ
  </Link>
  <Link to="/dat-lich" className="flex items-center gap-1.5 px-3 py-2 rounded-lg font-semibold">
    Đặt lịch khám
  </Link>
  <button
    type="button"
    onClick={() => window.dispatchEvent(new Event('mo-chat-ai'))}
    className="flex items-center gap-1.5 px-3 py-2 rounded-lg font-semibold"
  >
  
  </button>
  <Link to="/tra-cuu" className="flex items-center gap-1.5 px-3 py-2 rounded-lg font-semibold">
    Lịch sử khám
  </Link>
</nav>
</nav>

          {/* User actions */}
          <div className="flex items-center gap-3">
            {isAuthenticated ? (
              <div className="flex items-center gap-3">
                {user?.vaiTro === 'Admin' && (
                  <Link to="/admin" className="hidden sm:flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold bg-amber-50 text-black border border-amber-200 rounded-lg hover:bg-amber-100 transition">
                    <Shield className="w-3.5 h-3.5" />
                    Quản trị
                  </Link>
                )}
                {user?.vaiTro === 'BacSi' && (
                  <Link to="/doctor" className="hidden sm:flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold bg-emerald-50 text-black border border-emerald-200 rounded-lg hover:bg-emerald-100 transition">
                    <Stethoscope className="w-3.5 h-3.5" />
                    Bác sĩ
                  </Link>
                )}
                {user?.vaiTro === 'TiepTan' && (
                  <Link to="/tiep-tan" className="hidden sm:flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold bg-blue-50 text-black border border-blue-200 rounded-lg hover:bg-blue-100 transition">
                    <ClipboardList className="w-3.5 h-3.5" />
                    Tiếp tân
                  </Link>
                )}
                <div className="flex items-center gap-2 text-sm text-black font-medium">
                  <div className="w-8 h-8 rounded-full bg-sky-100 text-black flex items-center justify-center font-bold text-xs">
                    {user?.hoTen?.charAt(0) || 'U'}
                  </div>
                  <span className="hidden sm:inline">{user?.hoTen}</span>
                </div>
                <button
                  onClick={handleLogout}
                  title="Đăng xuất"
                  className="p-2 text-black hover:text-rose-600 hover:bg-rose-50 rounded-lg transition"
                >
                  <LogOut className="w-4 h-4" />
                </button>
              </div>
            ) : (
              <div className="flex items-center gap-2">
                <Link
                  to="/dang-nhap?tab=register"
                  className="hidden sm:inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-black bg-sky-50 hover:bg-sky-100 rounded-xl transition border border-sky-200"
                >
                  Đăng ký
                </Link>
                <Link
                  to="/dang-nhap"
                  className="flex items-center gap-1.5 px-3.5 py-1.5 text-xs font-semibold text-white bg-sky-500 hover:bg-sky-600 rounded-xl shadow-sm transition"
                >
                  <LogIn className="w-3.5 h-3.5" />
                  Đăng nhập
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  );
}