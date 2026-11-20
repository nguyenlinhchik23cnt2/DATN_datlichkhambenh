import React, { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { LogIn, UserPlus, Lock, User, Phone, Mail, MapPin, Calendar, AlertCircle, CheckCircle2, Activity, Eye, EyeOff } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

// Ô nhập mật khẩu có nút xem/ẩn. Mỗi ô tự quản lý trạng thái hiện/ẩn của riêng nó.
function PasswordInput({ value, onChange, placeholder, compact = false }) {
  const [show, setShow] = useState(false);

  return (
    <div className="relative">
      <Lock className={`w-4 h-4 text-slate-400 absolute top-1/2 -translate-y-1/2 ${compact ? 'left-3' : 'left-3.5'}`} />
      <input
        type={show ? 'text' : 'password'}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        className={`w-full rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-sky-400 ${
          compact ? 'pl-9 pr-10 py-2' : 'pl-10 pr-11 py-2.5 text-sm'
        }`}
        required
      />
      <button
        type="button"
        onClick={() => setShow((s) => !s)}
        aria-label={show ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
        title={show ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
        className={`absolute top-1/2 -translate-y-1/2 p-1 rounded-lg text-slate-400 hover:text-sky-600 hover:bg-sky-50 transition ${
          compact ? 'right-2' : 'right-2.5'
        }`}
      >
        {show ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
      </button>
    </div>
  );
}

export default function DangNhapPage() {
  const [searchParams] = useSearchParams();
  const initTab = searchParams.get('tab') === 'register' ? 'register' : 'login';
  const redirectParam = searchParams.get('redirect');

  const [activeTab, setActiveTab] = useState(initTab);

  // Form Đăng nhập
  const [loginUsername, setLoginUsername] = useState('');
  const [loginPassword, setLoginPassword] = useState('');

  // Form Đăng ký Bệnh nhân
  const [regForm, setRegForm] = useState({
    tenDangNhap: '',
    matKhau: '',
    xacNhanMatKhau: '',
    hoTen: '',
    soDienThoai: '',
    email: '',
    ngaySinh: '',
    gioiTinh: 'Nam',
    diaChi: '',
  });

  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const { login, register } = useAuth();
  const navigate = useNavigate();

  const handleNavigateAfterAuth = (user) => {
    if (redirectParam) {
      navigate(redirectParam);
      return;
    }
    const role = user?.vaiTro;
    if (role === 'Admin') {
      navigate('/admin');
    } else if (role === 'BacSi' || role === 'TiepTan') {
      navigate('/doctor');
    } else {
      navigate('/');
    }
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setErrorMsg('');
    setSuccessMsg('');

    const res = await login(loginUsername, loginPassword);
    setLoading(false);

    if (res.success) {
      handleNavigateAfterAuth(res.user);
    } else {
      setErrorMsg(res.message || 'Tên đăng nhập hoặc mật khẩu không chính xác!');
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setErrorMsg('');
    setSuccessMsg('');

    // Kiểm tra mật khẩu xác nhận
    if (regForm.matKhau !== regForm.xacNhanMatKhau) {
      setErrorMsg('Mật khẩu xác nhận không trùng khớp!');
      return;
    }
    if (regForm.matKhau.length < 6) {
      setErrorMsg('Mật khẩu tối thiểu phải từ 6 ký tự!');
      return;
    }
    if (!regForm.soDienThoai.trim()) {
      setErrorMsg('Vui lòng nhập số điện thoại liên hệ!');
      return;
    }

    setLoading(true);
    const payload = {
      tenDangNhap: regForm.tenDangNhap.trim(),
      matKhau: regForm.matKhau,
      hoTen: regForm.hoTen.trim(),
      soDienThoai: regForm.soDienThoai.trim(),
      email: regForm.email.trim() || null,
      ngaySinh: regForm.ngaySinh || null,
      gioiTinh: regForm.gioiTinh,
      diaChi: regForm.diaChi.trim() || null,
    };

    const res = await register(payload);
    setLoading(false);

    if (res.success) {
      setSuccessMsg('Đăng ký tài khoản bệnh nhân thành công! Đang chuyển hướng...');
      setTimeout(() => {
        handleNavigateAfterAuth(res.user);
      }, 1000);
    } else {
      setErrorMsg(res.message || 'Đăng ký không thành công, vui lòng kiểm tra lại!');
    }
  };

  return (
    <div className="min-h-[80vh] flex items-center justify-center px-4 py-10">
      <div className="max-w-lg w-full bg-white rounded-3xl border border-slate-200 shadow-xl p-8 space-y-6">

        {/* Header */}
        <div className="text-center space-y-2">
          <div className="w-12 h-12 bg-sky-100 text-sky-600 rounded-2xl flex items-center justify-center mx-auto mb-2">
            <Activity className="w-6 h-6" />
          </div>
          <h1 className="text-2xl font-black text-slate-800">Cổng Dịch Vụ Khám Bệnh</h1>
          <p className="text-slate-500 text-xs">
            {redirectParam ? (
              <span className="text-sky-700 font-semibold bg-sky-50 px-2.5 py-1 rounded-full border border-sky-200">
                👉 Vui lòng Đăng nhập hoặc Đăng ký để hoàn tất đặt lịch khám
              </span>
            ) : (
              'Dành cho Bệnh nhân, Bác sĩ & Nhân viên phòng khám'
            )}
          </p>
        </div>

        {/* Tab Switcher */}
        <div className="grid grid-cols-2 p-1 bg-slate-100 rounded-2xl text-xs font-bold text-slate-600">
          <button
            type="button"
            onClick={() => {
              setActiveTab('login');
              setErrorMsg('');
            }}
            className={`py-2.5 rounded-xl transition flex items-center justify-center gap-1.5 ${
              activeTab === 'login'
                ? 'bg-white text-sky-700 shadow-sm'
                : 'hover:text-slate-900'
            }`}
          >
            <LogIn className="w-4 h-4" />
            Đăng Nhập
          </button>
          <button
            type="button"
            onClick={() => {
              setActiveTab('register');
              setErrorMsg('');
            }}
            className={`py-2.5 rounded-xl transition flex items-center justify-center gap-1.5 ${
              activeTab === 'register'
                ? 'bg-white text-sky-700 shadow-sm'
                : 'hover:text-slate-900'
            }`}
          >
            <UserPlus className="w-4 h-4" />
            Đăng Ký Bệnh Nhân
          </button>
        </div>

        {/* Thông báo lỗi / thành công */}
        {errorMsg && (
          <div className="p-3.5 rounded-2xl bg-rose-50 border border-rose-200 text-rose-700 text-xs flex items-center gap-2">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{errorMsg}</span>
          </div>
        )}
        {successMsg && (
          <div className="p-3.5 rounded-2xl bg-emerald-50 border border-emerald-200 text-emerald-700 text-xs flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4 shrink-0" />
            <span>{successMsg}</span>
          </div>
        )}

        {/* Tab 1: Đăng nhập */}
        {activeTab === 'login' && (
          <form onSubmit={handleLogin} className="space-y-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1.5">Tên Đăng Nhập</label>
              <div className="relative">
                <User className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  value={loginUsername}
                  onChange={(e) => setLoginUsername(e.target.value)}
                  placeholder="Nhập tên đăng nhập..."
                  className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-300 text-sm focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-sky-400"
                  required
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1.5">Mật Khẩu</label>
              <PasswordInput
                value={loginPassword}
                onChange={(e) => setLoginPassword(e.target.value)}
                placeholder="Nhập mật khẩu..."
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-3.5 rounded-xl bg-emerald-500 hover:bg-emerald-600 text-white font-bold text-sm shadow-md transition disabled:opacity-50 flex items-center justify-center gap-2"
            >
              <LogIn className="w-4 h-4" />
              {loading ? 'Đang Đăng Nhập...' : 'Đăng Nhập'}
            </button>

            <div className="text-center text-xs text-slate-500 pt-2 border-t border-slate-100">
              Chưa có tài khoản bệnh nhân?{' '}
              <button
                type="button"
                onClick={() => setActiveTab('register')}
                className="text-sky-600 font-bold hover:underline"
              >
                Đăng ký ngay tại đây
              </button>
            </div>
          </form>
        )}

        {/* Tab 2: Đăng ký Bệnh nhân */}
        {activeTab === 'register' && (
          <form onSubmit={handleRegister} className="space-y-3.5 text-xs">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div>
                <label className="block font-bold text-slate-700 mb-1">Tên Đăng Nhập *</label>
                <div className="relative">
                  <User className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                  <input
                    type="text"
                    value={regForm.tenDangNhap}
                    onChange={(e) => setRegForm({ ...regForm, tenDangNhap: e.target.value })}
                    placeholder="vd: benhnhan_an"
                    className="w-full pl-9 pr-3 py-2 rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-sky-400"
                    required
                  />
                </div>
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">Họ Và Tên *</label>
                <input
                  type="text"
                  value={regForm.hoTen}
                  onChange={(e) => setRegForm({ ...regForm, hoTen: e.target.value })}
                  placeholder="vd: Nguyễn Văn An"
                  className="w-full px-3 py-2 rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-sky-400"
                  required
                />
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div>
                <label className="block font-bold text-slate-700 mb-1">Mật Khẩu *</label>
                <PasswordInput
                  compact
                  value={regForm.matKhau}
                  onChange={(e) => setRegForm({ ...regForm, matKhau: e.target.value })}
                  placeholder="Tối thiểu 6 ký tự"
                />
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">Xác Nhận Mật Khẩu *</label>
                <PasswordInput
                  compact
                  value={regForm.xacNhanMatKhau}
                  onChange={(e) => setRegForm({ ...regForm, xacNhanMatKhau: e.target.value })}
                  placeholder="Nhập lại mật khẩu"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div>
                <label className="block font-bold text-slate-700 mb-1">Số Điện Thoại *</label>
                <div className="relative">
                  <Phone className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                  <input
                    type="tel"
                    value={regForm.soDienThoai}
                    onChange={(e) => setRegForm({ ...regForm, soDienThoai: e.target.value })}
                    placeholder="0912 345 678"
                    className="w-full pl-9 pr-3 py-2 rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-sky-400"
                    required
                  />
                </div>
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">Email</label>
                <div className="relative">
                  <Mail className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                  <input
                    type="email"
                    value={regForm.email}
                    onChange={(e) => setRegForm({ ...regForm, email: e.target.value })}
                    placeholder="an@gmail.com"
                    className="w-full pl-9 pr-3 py-2 rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-sky-400"
                  />
                </div>
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div>
                <label className="block font-bold text-slate-700 mb-1">Ngày Sinh</label>
                <div className="relative">
                  <Calendar className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                  <input
                    type="date"
                    value={regForm.ngaySinh}
                    onChange={(e) => setRegForm({ ...regForm, ngaySinh: e.target.value })}
                    className="w-full pl-9 pr-3 py-2 rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-sky-400"
                  />
                </div>
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">Giới Tính</label>
                <select
                  value={regForm.gioiTinh}
                  onChange={(e) => setRegForm({ ...regForm, gioiTinh: e.target.value })}
                  className="w-full px-3 py-2 rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-sky-400 bg-white"
                >
                  <option value="Nam">Nam</option>
                  <option value="Nữ">Nữ</option>
                  <option value="Khác">Khác</option>
                </select>
              </div>
            </div>

            <div>
              <label className="block font-bold text-slate-700 mb-1">Địa Chỉ Thường Trú</label>
              <div className="relative">
                <MapPin className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
                <input
                  type="text"
                  value={regForm.diaChi}
                  onChange={(e) => setRegForm({ ...regForm, diaChi: e.target.value })}
                  placeholder="Số nhà, đường, phường/xã, quận/huyện..."
                  className="w-full pl-9 pr-3 py-2 rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-sky-400"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-3.5 mt-2 rounded-xl bg-emerald-500 hover:bg-emerald-600 text-white font-bold text-sm shadow-md transition disabled:opacity-50 flex items-center justify-center gap-2"
            >
              <UserPlus className="w-4 h-4" />
              {loading ? 'Đang Tạo Hồ Sơ Bệnh Nhân...' : 'Tạo Tài Khoản & Tiếp Tục'}
            </button>

            <div className="text-center text-slate-500 pt-2 border-t border-slate-100">
              Đã có tài khoản?{' '}
              <button
                type="button"
                onClick={() => setActiveTab('login')}
                className="text-sky-600 font-bold hover:underline"
              >
                Đăng nhập ngay
              </button>
            </div>
          </form>
        )}

      </div>
    </div>
  );
}