import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import { CheckCircle2, AlertCircle, Sun, Moon, Lock, ShieldCheck, ArrowRight } from 'lucide-react';
import api from '../api/client';
import { useAuth } from '../context/AuthContext';

// Ngày theo giờ máy người dùng (yyyy-MM-dd). Không dùng toISOString vì nó trả về giờ UTC,
// nên ở Việt Nam trước 7h sáng sẽ ra ngày hôm trước.
const toLocalDate = (d = new Date()) => d.toLocaleDateString('en-CA');

const toLocalTime = (d = new Date()) =>
  d.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' });

const formatNgay = (value) => {
  const [y, m, d] = String(value || '').split('-');
  return y && m && d ? `${d}/${m}/${y}` : value;
};

function SlotGroup({ title, icon, titleClass, slots, selectedSlot, onSelect, isPast }) {
  if (slots.length === 0) return null;

  return (
    <div className="space-y-2">
      <div className={`flex items-center gap-1.5 text-xs font-bold ${titleClass}`}>
        {icon}
        <span>{title}</span>
      </div>
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
        {slots.map((s) => {
          const startStr = s.gioBatDau?.slice(0, 5);
          const endStr = s.gioKetThuc?.slice(0, 5);
          const daQua = isPast(s);
          const khongDat = !s.conTrong || daQua;
          const isSelected = selectedSlot?.maLich === s.maLich && selectedSlot?.gioBatDau === s.gioBatDau;

          if (khongDat) {
            return (
              <div
                key={`${s.maLich}-${s.gioBatDau}`}
                className="p-2.5 rounded-xl border border-slate-200 bg-slate-100 text-slate-400 text-center text-xs line-through cursor-not-allowed select-none"
                title={daQua ? 'Đã qua giờ' : 'Đã có người đặt'}
              >
                {startStr} - {endStr}
              </div>
            );
          }

          return (
            <button
              type="button"
              key={`${s.maLich}-${s.gioBatDau}`}
              onClick={() => onSelect(s)}
              className={`p-2.5 rounded-xl border text-center text-xs font-bold transition ${
                isSelected
                  ? 'border-cyan-600 bg-cyan-600 text-white shadow-md'
                  : 'border-cyan-200 bg-cyan-50/70 text-cyan-800 hover:bg-cyan-100'
              }`}
            >
              {startStr} - {endStr}
              <span className={`block text-[10px] mt-0.5 font-normal ${isSelected ? 'text-cyan-100' : 'text-cyan-600'}`}>
                Còn trống
              </span>
            </button>
          );
        })}
      </div>
    </div>
  );
}

export default function DatLichPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();

  // Chỉ tài khoản bệnh nhân mới đặt được lịch (backend cũng chặn các vai trò khác)
  const laBenhNhan = isAuthenticated && user?.vaiTro === 'BenhNhan';

  const initChuyenKhoa = searchParams.get('chuyenKhoa');
  const initBacSi = searchParams.get('bacSi');

  const [chuyenKhoas, setChuyenKhoas] = useState([]);
  const [bacSis, setBacSis] = useState([]);
  const [selectedChuyenKhoa, setSelectedChuyenKhoa] = useState(initChuyenKhoa || '');
  const [selectedBacSi, setSelectedBacSi] = useState(initBacSi || '');
  const [ngayKham, setNgayKham] = useState(toLocalDate());
  const [slots, setSlots] = useState([]);
  const [selectedSlot, setSelectedSlot] = useState(null);
  const [slotsVersion, setSlotsVersion] = useState(0); // tăng lên để tải lại danh sách giờ

  const [hoTen, setHoTen] = useState('');
  const [soDienThoai, setSoDienThoai] = useState('');
  const [lyDoKham, setLyDoKham] = useState('');

  const [loadingSlots, setLoadingSlots] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [successResult, setSuccessResult] = useState(null);
  const [errorMsg, setErrorMsg] = useState('');

  // Tự động điền thông tin từ tài khoản
  useEffect(() => {
    if (user) {
      if (user.hoTen) setHoTen(user.hoTen);
      if (user.soDienThoai) setSoDienThoai(user.soDienThoai);
    }
  }, [user]);

  // Danh sách chuyên khoa
  useEffect(() => {
    api.get('/chuyen-khoa/dang-hoat-dong')
      .then((res) => setChuyenKhoas(res.data || []))
      .catch(console.error);
  }, []);

  // Danh sách bác sĩ. Giữ bác sĩ đang chọn nếu còn trong danh sách, nếu không thì chọn người đầu tiên.
  useEffect(() => {
    const params = selectedChuyenKhoa ? { maChuyenKhoa: selectedChuyenKhoa } : {};
    api.get('/bac-si/danh-sach-chi-tiet', { params })
      .then((res) => {
        const list = res.data || [];
        setBacSis(list);
        setSelectedBacSi((prev) => {
          if (prev && list.some((b) => String(b.maBacSi) === String(prev))) return prev;
          return list.length > 0 ? String(list[0].maBacSi) : '';
        });
      })
      .catch(console.error);
  }, [selectedChuyenKhoa]);

  // Khung giờ 30 phút theo bác sĩ và ngày
  useEffect(() => {
    if (!selectedBacSi || !ngayKham) {
      setSlots([]);
      setSelectedSlot(null);
      return;
    }

    let huy = false;
    setLoadingSlots(true);
    setSelectedSlot(null);

    api.get(`/lich-lam-viec/bac-si/${selectedBacSi}/slots`, { params: { ngay: ngayKham } })
      .then((res) => { if (!huy) setSlots(res.data || []); })
      .catch((err) => {
        console.warn('Lỗi khi tải khung giờ khám:', err);
        if (!huy) setSlots([]);
      })
      .finally(() => { if (!huy) setLoadingSlots(false); });

    return () => { huy = true; };
  }, [selectedBacSi, ngayKham, slotsVersion]);

  const setQuickDate = (offsetDays) => {
    const d = new Date();
    d.setDate(d.getDate() + offsetDays);
    setNgayKham(toLocalDate(d));
  };

  // Khung giờ đã qua trong ngày hôm nay thì không cho chọn (backend cũng chặn)
  const isPast = (slot) =>
    ngayKham === toLocalDate() && (slot.gioBatDau?.slice(0, 5) || '') <= toLocalTime();

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!isAuthenticated) {
      navigate('/dang-nhap?redirect=/dat-lich');
      return;
    }

    if (!laBenhNhan) {
      setErrorMsg('Chỉ tài khoản bệnh nhân mới đặt được lịch khám!');
      return;
    }

    if (!user?.maBenhNhan) {
      setErrorMsg('Tài khoản chưa liên kết hồ sơ bệnh nhân. Vui lòng đăng xuất và đăng nhập lại!');
      return;
    }

    if (!selectedSlot) {
      setErrorMsg('Vui lòng chọn một khung giờ khám còn trống!');
      return;
    }

    const patientName = hoTen.trim() || user?.hoTen;
    const patientPhone = soDienThoai.trim() || user?.soDienThoai;

    if (!patientName || !patientPhone) {
      setErrorMsg('Vui lòng điền đầy đủ họ tên và số điện thoại liên hệ!');
      return;
    }

    setSubmitting(true);
    setErrorMsg('');
    try {
      const payload = {
        maBenhNhan: user.maBenhNhan,
        maLich: selectedSlot.maLich,
        gioBatDau: selectedSlot.gioBatDau,
        tenBenhNhan: patientName,
        // Lý do khám giữ sạch để bác sĩ thấy đúng triệu chứng; SĐT đưa vào ghi chú
        lyDoKham: lyDoKham.trim() || 'Khám bệnh theo lịch hẹn',
        ghiChu: `SĐT liên hệ: ${patientPhone}`,
      };

      const res = await api.post('/lich-hen/dat-lich', payload);
      setSuccessResult(res.data);
      setSlotsVersion((v) => v + 1);
    } catch (err) {
      setErrorMsg(err.response?.data?.message || 'Đặt lịch không thành công, vui lòng thử lại!');
      // Giờ vừa có người khác đặt: tải lại để cập nhật khung giờ
      setSlotsVersion((v) => v + 1);
    } finally {
      setSubmitting(false);
    }
  };

  const slotsSang = slots.filter((s) => parseInt(s.gioBatDau?.slice(0, 2), 10) < 12);
  const slotsChieu = slots.filter((s) => parseInt(s.gioBatDau?.slice(0, 2), 10) >= 12);

  if (successResult) {
    return (
      <div className="max-w-xl mx-auto my-12 p-8 bg-white rounded-3xl border border-emerald-200 shadow-xl text-center space-y-6">
        <div className="w-16 h-16 bg-emerald-100 text-emerald-600 rounded-full flex items-center justify-center mx-auto">
          <CheckCircle2 className="w-10 h-10" />
        </div>
        <div>
          <h2 className="text-2xl font-extrabold text-slate-800">Đặt Lịch Khám Thành Công!</h2>
          <p className="text-slate-500 text-sm mt-1">Cảm ơn bạn đã tin tưởng dịch vụ phòng khám của chúng tôi.</p>
        </div>

        <div className="p-6 bg-emerald-50/70 border border-emerald-200/60 rounded-2xl text-left space-y-3">
          <div className="flex justify-between items-center pb-3 border-b border-emerald-200/60">
            <span className="text-xs font-semibold text-slate-500 uppercase">Mã Đặt Lịch:</span>
            <span className="text-lg font-black text-emerald-700 tracking-wider font-mono">{successResult.maDatLich}</span>
          </div>
          <div className="text-xs space-y-1.5 text-slate-700">
            <div><strong className="text-slate-900">Bệnh nhân:</strong> {successResult.tenBenhNhan || hoTen}</div>
            <div><strong className="text-slate-900">Khung giờ:</strong> {successResult.gioBatDau?.slice(0, 5)} - {successResult.gioKetThuc?.slice(0, 5)}</div>
            <div><strong className="text-slate-900">Ngày khám:</strong> {formatNgay(ngayKham)}</div>
            <div><strong className="text-slate-900">Trạng thái:</strong> <span className="text-amber-600 font-bold">Chờ phòng khám duyệt</span></div>
          </div>
        </div>

        <div className="text-xs text-slate-500 bg-slate-50 p-4 rounded-xl">
          💡 Vui lòng lưu lại <strong>Mã Đặt Lịch ({successResult.maDatLich})</strong> để tra cứu hoặc xuất trình khi đến làm thủ tục tại quầy tiếp tân.
        </div>

        <div className="flex gap-3 justify-center">
          <button
            onClick={() => {
              setSuccessResult(null);
              setSelectedSlot(null);
              setLyDoKham('');
            }}
            className="px-5 py-2.5 rounded-xl border border-slate-300 text-xs font-bold text-slate-700 hover:bg-slate-100 transition"
          >
            Đặt lịch khám khác
          </button>
          <Link
            to="/tra-cuu"
            className="px-5 py-2.5 rounded-xl bg-cyan-600 text-white text-xs font-bold hover:bg-cyan-700 transition"
          >
            Tra cứu lịch hẹn
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="mb-6 text-center space-y-2">
        <h1 className="text-3xl font-black text-slate-900">Đặt Lịch Khám Trực Tuyến</h1>
        <p className="text-slate-500 text-sm">
          Chọn bác sĩ, chuyên khoa và khung giờ khám 30 phút nhanh chóng, tiện lợi
        </p>
      </div>

      {/* Banner trạng thái đăng nhập */}
      {!isAuthenticated ? (
        <div className="mb-6 p-4 rounded-2xl bg-amber-50 border border-amber-200/90 text-amber-900 shadow-sm flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-amber-100 text-amber-600 flex items-center justify-center shrink-0">
              <Lock className="w-5 h-5" />
            </div>
            <div>
              <div className="font-bold text-xs sm:text-sm text-amber-900">
                Bắt buộc Đăng nhập hoặc Đăng ký để đặt lịch khám
              </div>
              <div className="text-[11px] text-amber-700">
                Quý khách vui lòng đăng nhập tài khoản bệnh nhân để hệ thống cấp mã hẹn y tế và ghi nhận hồ sơ khám.
              </div>
            </div>
          </div>
          <div className="flex items-center gap-2 w-full sm:w-auto">
            <Link
              to="/dang-nhap?redirect=/dat-lich"
              className="flex-1 sm:flex-none text-center px-4 py-2 bg-amber-600 hover:bg-amber-700 text-white font-bold rounded-xl text-xs transition shadow-sm"
            >
              Đăng Nhập
            </Link>
            <Link
              to="/dang-nhap?tab=register&redirect=/dat-lich"
              className="flex-1 sm:flex-none text-center px-4 py-2 bg-white hover:bg-amber-100/60 border border-amber-300 text-amber-900 font-bold rounded-xl text-xs transition shadow-sm"
            >
              Đăng Ký
            </Link>
          </div>
        </div>
      ) : !laBenhNhan ? (
        <div className="mb-6 p-4 rounded-2xl bg-amber-50 border border-amber-200 text-amber-900 text-xs flex items-center gap-3">
          <AlertCircle className="w-5 h-5 shrink-0 text-amber-600" />
          <span>
            Bạn đang đăng nhập bằng tài khoản <strong>{user?.hoTen}</strong> (không phải bệnh nhân).
            Chức năng đặt lịch chỉ dành cho tài khoản bệnh nhân.
          </span>
        </div>
      ) : (
        <div className="mb-6 p-4 rounded-2xl bg-emerald-50 border border-emerald-200 text-emerald-950 shadow-sm flex items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-emerald-100 text-emerald-600 flex items-center justify-center shrink-0">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <div className="font-bold text-xs sm:text-sm text-emerald-900">
                Tài khoản Bệnh nhân: <span className="text-emerald-700">{user.hoTen}</span>
              </div>
              <div className="text-[11px] text-emerald-700">
                {user.soDienThoai && <>Số điện thoại: <span className="font-semibold">{user.soDienThoai}</span></>}
                {user.maBenhNhan && (
                  <> • Mã bệnh nhân: <span className="font-mono font-bold">#BN-{user.maBenhNhan}</span></>
                )}
                {user.email && <> • Email: {user.email}</>}
              </div>
            </div>
          </div>
          <span className="hidden sm:inline-block px-3 py-1 bg-emerald-200/70 text-emerald-800 rounded-full text-[11px] font-bold">
            Đã sẵn sàng đặt lịch
          </span>
        </div>
      )}

      {errorMsg && (
        <div className="mb-6 p-4 rounded-2xl bg-rose-50 border border-rose-200 text-rose-700 text-xs flex items-center gap-2">
          <AlertCircle className="w-5 h-5 shrink-0" />
          <span>{errorMsg}</span>
        </div>
      )}

      <form onSubmit={handleSubmit} className="bg-white rounded-3xl border border-slate-200 shadow-sm p-6 sm:p-8 space-y-8">

        {/* Bước 1 */}
        <div>
          <h2 className="text-base font-bold text-slate-800 flex items-center gap-2 mb-4">
            <span className="w-6 h-6 rounded-full bg-cyan-600 text-white text-xs flex items-center justify-center font-bold">1</span>
            Chọn Chuyên Khoa & Bác Sĩ Phụ Trách
          </h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1.5">Chuyên Khoa</label>
              <select
                value={selectedChuyenKhoa}
                onChange={(e) => setSelectedChuyenKhoa(e.target.value)}
                className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-sm focus:outline-none focus:ring-2 focus:ring-cyan-500 bg-white"
              >
                <option value="">-- Tất cả các chuyên khoa --</option>
                {chuyenKhoas.map((ck) => (
                  <option key={ck.maChuyenKhoa} value={ck.maChuyenKhoa}>{ck.tenChuyenKhoa}</option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1.5">Bác Sĩ</label>
              <select
                value={selectedBacSi}
                onChange={(e) => setSelectedBacSi(e.target.value)}
                className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-sm focus:outline-none focus:ring-2 focus:ring-cyan-500 bg-white"
                required
              >
                <option value="">-- Chọn bác sĩ --</option>
                {bacSis.map((bs) => (
                  <option key={bs.maBacSi} value={bs.maBacSi}>
                    {bs.hocVi ? `${bs.hocVi} ` : 'BS. '}{bs.hoTen} ({bs.tenChuyenKhoa})
                  </option>
                ))}
              </select>
            </div>
          </div>
        </div>

        {/* Bước 2 */}
        <div>
          <h2 className="text-base font-bold text-slate-800 flex items-center gap-2 mb-4">
            <span className="w-6 h-6 rounded-full bg-cyan-600 text-white text-xs flex items-center justify-center font-bold">2</span>
            Chọn Ngày & Khung Giờ Khám (Slot 30 phút)
          </h2>

          <div className="space-y-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1.5">Ngày Khám Dự Kiến</label>
              <div className="flex flex-wrap items-center gap-2">
                <input
                  type="date"
                  value={ngayKham}
                  min={toLocalDate()}
                  onChange={(e) => e.target.value && setNgayKham(e.target.value)}
                  className="px-3.5 py-2 rounded-xl border border-slate-300 text-sm focus:outline-none focus:ring-2 focus:ring-cyan-500"
                  required
                />
                {[['Hôm nay', 0], ['Ngày mai', 1], ['Ngày kia', 2]].map(([nhan, offset]) => (
                  <button
                    key={offset}
                    type="button"
                    onClick={() => setQuickDate(offset)}
                    className="px-3 py-2 text-xs font-semibold rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 transition"
                  >
                    {nhan}
                  </button>
                ))}
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 mb-2">
                Khung Giờ Còn Trống Trong Ngày{' '}
                {selectedSlot && (
                  <span className="text-cyan-600 font-extrabold ml-2">
                    ✓ Đã chọn: {selectedSlot.gioBatDau?.slice(0, 5)} - {selectedSlot.gioKetThuc?.slice(0, 5)}
                  </span>
                )}
              </label>

              {loadingSlots ? (
                <div className="p-8 text-center text-xs text-slate-400 bg-slate-50 rounded-2xl">
                  Đang tải danh sách khung giờ trống...
                </div>
              ) : slots.length === 0 ? (
                <div className="p-6 text-center text-xs text-amber-700 bg-amber-50 border border-amber-200/60 rounded-2xl">
                  Bác sĩ chưa có ca trực vào ngày đã chọn. Vui lòng chọn ngày khám khác hoặc đổi bác sĩ!
                </div>
              ) : (
                <div className="space-y-4">
                  <SlotGroup
                    title="Ca Sáng"
                    icon={<Sun className="w-4 h-4 text-amber-500" />}
                    titleClass="text-amber-700"
                    slots={slotsSang}
                    selectedSlot={selectedSlot}
                    onSelect={setSelectedSlot}
                    isPast={isPast}
                  />
                  <SlotGroup
                    title="Ca Chiều"
                    icon={<Moon className="w-4 h-4 text-blue-500" />}
                    titleClass="text-blue-700"
                    slots={slotsChieu}
                    selectedSlot={selectedSlot}
                    onSelect={setSelectedSlot}
                    isPast={isPast}
                  />
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Bước 3 */}
        <div>
          <h2 className="text-base font-bold text-slate-800 flex items-center gap-2 mb-4">
            <span className="w-6 h-6 rounded-full bg-cyan-600 text-white text-xs flex items-center justify-center font-bold">3</span>
            Thông Tin Bệnh Nhân Đặt Lịch
          </h2>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1.5">Họ và Tên (*)</label>
              <input
                type="text"
                placeholder="Ví dụ: Nguyễn Văn An"
                value={hoTen}
                onChange={(e) => setHoTen(e.target.value)}
                className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-sm focus:outline-none focus:ring-2 focus:ring-cyan-500"
                required
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1.5">Số Điện Thoại Liên Hệ (*)</label>
              <input
                type="tel"
                placeholder="Ví dụ: 0912345678"
                value={soDienThoai}
                onChange={(e) => setSoDienThoai(e.target.value)}
                className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-sm focus:outline-none focus:ring-2 focus:ring-cyan-500"
                required
              />
            </div>
            <div className="sm:col-span-2">
              <label className="block text-xs font-bold text-slate-700 mb-1.5">Lý Do Khám / Triệu Chứng Ban Đầu</label>
              <textarea
                rows="3"
                placeholder="Mô tả sơ lược triệu chứng: đau đầu, ho sốt, tức ngực..."
                value={lyDoKham}
                onChange={(e) => setLyDoKham(e.target.value)}
                className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-sm focus:outline-none focus:ring-2 focus:ring-cyan-500"
              />
            </div>
          </div>
        </div>

        {/* Nút hành động */}
        {isAuthenticated ? (
          <button
            type="submit"
            disabled={submitting || !selectedSlot || !laBenhNhan}
            className="w-full py-4 rounded-2xl bg-cyan-600 text-white font-extrabold text-sm hover:bg-cyan-700 transition disabled:opacity-50 disabled:cursor-not-allowed shadow-lg shadow-cyan-600/20 flex items-center justify-center gap-2"
          >
            <CheckCircle2 className="w-5 h-5" />
            {submitting ? 'Đang Xử Lý Đặt Lịch...' : 'Xác Nhận Đặt Lịch Khám'}
          </button>
        ) : (
          <div className="space-y-3 pt-2">
            <Link
              to="/dang-nhap?redirect=/dat-lich"
              className="w-full py-4 rounded-2xl bg-amber-600 text-white font-extrabold text-sm hover:bg-amber-700 transition shadow-lg shadow-amber-600/20 flex items-center justify-center gap-2"
            >
              <Lock className="w-5 h-5" />
              Đăng Nhập Hoặc Đăng Ký Để Đặt Lịch Khám
              <ArrowRight className="w-4 h-4 ml-1" />
            </Link>
            <p className="text-center text-xs text-slate-500">
              Chưa có tài khoản?{' '}
              <Link to="/dang-nhap?tab=register&redirect=/dat-lich" className="text-cyan-600 font-bold hover:underline">
                Đăng ký tài khoản bệnh nhân miễn phí
              </Link>
            </p>
          </div>
        )}
      </form>
    </div>
  );
}