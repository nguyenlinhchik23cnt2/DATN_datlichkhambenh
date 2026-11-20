import React, { useState, useEffect, useMemo } from 'react';
import { Stethoscope, Plus, Trash2, CheckCircle2, AlertCircle } from 'lucide-react';
import api from '../../api/client';
import { useAuth } from '../../context/AuthContext';

const emptyThuoc = () => ({ tenThuoc: '', soLuong: 1, lieuDung: '', cachDung: '' });

// Ngày hôm nay theo giờ máy người dùng, định dạng yyyy-MM-dd
const homNay = () => new Date().toLocaleDateString('en-CA');

export default function DoctorDashboard() {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState('lichHen'); // 'lichHen' | 'khamBenh'

  const [ngay, setNgay] = useState(homNay());
  const [lichHens, setLichHens] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [msg, setMsg] = useState({ type: '', text: '' });

  // State cho màn hình khám bệnh
  const [selectedLich, setSelectedLich] = useState(null);
  const [trieuChung, setTrieuChung] = useState('');
  const [chanDoan, setChanDoan] = useState('');
  const [loiDan, setLoiDan] = useState('');
  const [ghiChuThuoc, setGhiChuThuoc] = useState('');
  const [danhSachThuoc, setDanhSachThuoc] = useState([emptyThuoc()]);
  const [submittingKham, setSubmittingKham] = useState(false);

  // Bác sĩ chỉ lấy lịch của chính mình theo ngày
  const fetchLichHen = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await api.get('/lich-hen/bac-si-cua-toi', { params: { ngay } });
      setLichHens(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      console.error(err);
      setLichHens([]);
      setError(err.response?.data?.message || 'Không thể tải danh sách lịch hẹn!');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLichHen();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [ngay]);

  // Bác sĩ chỉ thấy lịch đã được lễ tân duyệt hoặc đã khám xong
  const lichHienThi = useMemo(
    () => lichHens.filter((lh) => lh.trangThai === 'DaDuyet' || lh.trangThai === 'HoanThanh'),
    [lichHens]
  );

  const soChoKham = lichHienThi.filter((lh) => lh.trangThai === 'DaDuyet').length;
  const soDaKham = lichHienThi.filter((lh) => lh.trangThai === 'HoanThanh').length;

  const startKhamBenh = (lh) => {
    setMsg({ type: '', text: '' });
    setSelectedLich(lh);
    setTrieuChung(lh.lyDoKham || '');
    setChanDoan('');
    setLoiDan('');
    setGhiChuThuoc('');
    setDanhSachThuoc([emptyThuoc()]);
    setActiveTab('khamBenh');
  };

  const addThuocRow = () => setDanhSachThuoc([...danhSachThuoc, emptyThuoc()]);

  const removeThuocRow = (idx) => setDanhSachThuoc(danhSachThuoc.filter((_, i) => i !== idx));

  const handleThuocChange = (index, field, value) => {
    setDanhSachThuoc(
      danhSachThuoc.map((t, i) => (i === index ? { ...t, [field]: value } : t))
    );
  };

  const handleHoanThanhKham = async (e) => {
    e.preventDefault();
    if (!chanDoan.trim()) {
      alert('Vui lòng nhập chẩn đoán bệnh!');
      return;
    }
    setSubmittingKham(true);
    try {
      const validThuoc = danhSachThuoc.filter((t) => t.tenThuoc.trim() !== '');
      await api.post('/kham-benh/hoan-thanh', {
        maLichHen: selectedLich.maLichHen,
        trieuChung,
        chanDoan,
        loiDanBacSi: loiDan,
        ghiChuDonThuoc: ghiChuThuoc,
        danhSachThuoc: validThuoc,
      });
      setMsg({ type: 'success', text: 'Đã lưu kết quả khám và đơn thuốc!' });
      setSelectedLich(null);
      setActiveTab('lichHen');
      fetchLichHen();
    } catch (err) {
      setMsg({
        type: 'error',
        text: 'Lỗi khi lưu kết quả khám: ' + (err.response?.data?.message || err.message),
      });
    } finally {
      setSubmittingKham(false);
    }
  };

  return (
    <div className="max-w-7xl mx-auto my-8 px-4 sm:px-6 lg:px-8 space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4 bg-white p-6 rounded-3xl border border-slate-200 shadow-sm">
        <div className="flex items-center gap-3">
          <div className="p-3 bg-sky-100 text-sky-700 rounded-2xl">
            <Stethoscope className="w-7 h-7" />
          </div>
          <div>
            <h1 className="text-xl font-extrabold text-slate-800">Cổng Làm Việc Bác Sĩ</h1>
            <p className="text-xs text-slate-500 font-medium">Xin chào, {user?.hoTen}</p>
          </div>
        </div>

        <div className="flex bg-slate-100 p-1 rounded-2xl text-xs font-bold">
          <button
            onClick={() => setActiveTab('lichHen')}
            className={`px-4 py-2 rounded-xl transition ${activeTab === 'lichHen' ? 'bg-white text-sky-700 shadow-sm' : 'text-slate-600 hover:text-slate-900'}`}
          >
            Lịch Khám Của Tôi
          </button>
          <button
            onClick={() => setActiveTab('khamBenh')}
            className={`px-4 py-2 rounded-xl transition ${activeTab === 'khamBenh' ? 'bg-white text-sky-700 shadow-sm' : 'text-slate-600 hover:text-slate-900'}`}
          >
            Phòng Khám & Kê Đơn {selectedLich && `(${selectedLich.tenBenhNhan})`}
          </button>
        </div>
      </div>

      {msg.text && (
        <div className={`p-4 rounded-2xl text-xs font-medium flex items-center gap-2 ${msg.type === 'success' ? 'bg-emerald-50 text-emerald-800 border border-emerald-200' : 'bg-rose-50 text-rose-800 border border-rose-200'}`}>
          {msg.type === 'success' ? <CheckCircle2 className="w-4 h-4 shrink-0" /> : <AlertCircle className="w-4 h-4 shrink-0" />}
          <span>{msg.text}</span>
        </div>
      )}

      {activeTab === 'lichHen' && (
        <div className="bg-white rounded-3xl border border-slate-200 shadow-sm overflow-hidden">
          <div className="p-6 border-b border-slate-100 flex flex-wrap justify-between items-center gap-3">
            <div>
              <h2 className="text-base font-bold text-slate-800">Danh Sách Bệnh Nhân Cần Khám</h2>
              <p className="text-xs text-slate-500 mt-1">
                Chờ khám: {soChoKham} · Đã khám: {soDaKham}
              </p>
            </div>
            <div className="flex items-center gap-3">
              <input
                type="date"
                value={ngay}
                onChange={(e) => e.target.value && setNgay(e.target.value)}
                className="border border-slate-300 rounded-lg px-3 py-1.5 text-xs focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-sky-400"
              />
              <button
                onClick={() => setNgay(homNay())}
                className="text-xs font-semibold text-slate-600 hover:underline"
              >
                Hôm nay
              </button>
              <button
                onClick={fetchLichHen}
                className="text-xs font-semibold text-sky-700 hover:underline"
              >
                Làm mới
              </button>
            </div>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 text-slate-500 font-bold border-b border-slate-200">
                <tr>
                  <th className="p-4">Mã Đặt Lịch</th>
                  <th className="p-4">Bệnh Nhân</th>
                  <th className="p-4">Khung Giờ (30p)</th>
                  <th className="p-4">Lý Do Khám</th>
                  <th className="p-4">Trạng Thái</th>
                  <th className="p-4 text-center">Thao Tác</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {loading ? (
                  <tr><td colSpan="6" className="p-8 text-center text-slate-400">Đang tải dữ liệu...</td></tr>
                ) : error ? (
                  <tr><td colSpan="6" className="p-8 text-center text-rose-600">{error}</td></tr>
                ) : lichHienThi.length === 0 ? (
                  <tr><td colSpan="6" className="p-8 text-center text-slate-400">Không có bệnh nhân nào trong ngày này</td></tr>
                ) : (
                  lichHienThi.map((lh) => (
                    <tr key={lh.maLichHen} className="hover:bg-sky-50/50 transition">
                      <td className="p-4 font-mono font-bold text-slate-700">{lh.maDatLich}</td>
                      <td className="p-4 font-bold text-slate-900">{lh.tenBenhNhan}</td>
                      <td className="p-4 text-slate-600 font-medium">
                        {lh.gioBatDau?.slice(0, 5)} - {lh.gioKetThuc?.slice(0, 5)}
                      </td>
                      <td className="p-4 text-slate-500 max-w-xs truncate">{lh.lyDoKham || '---'}</td>
                      <td className="p-4">
                        {lh.trangThai === 'DaDuyet' && <span className="px-2.5 py-1 bg-emerald-100 text-emerald-800 rounded-full font-bold text-[10px]">Chờ khám</span>}
                        {lh.trangThai === 'HoanThanh' && <span className="px-2.5 py-1 bg-sky-100 text-sky-800 rounded-full font-bold text-[10px]">Đã khám</span>}
                      </td>
                      <td className="p-4 text-center">
                        {lh.trangThai === 'DaDuyet' && (
                          <button
                            onClick={() => startKhamBenh(lh)}
                            className="px-3 py-1.5 bg-emerald-500 text-white rounded-lg font-bold text-[11px] hover:bg-emerald-600 transition"
                          >
                            Khám Bệnh
                          </button>
                        )}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {activeTab === 'khamBenh' && (
        <div className="bg-white rounded-3xl border border-slate-200 shadow-sm p-6 sm:p-8 space-y-6">
          {!selectedLich ? (
            <div className="text-center py-12 text-slate-400 space-y-3">
              <p>Vui lòng chọn bệnh nhân ở tab "Lịch Khám Của Tôi" và bấm nút <strong>Khám Bệnh</strong>.</p>
              <button
                onClick={() => setActiveTab('lichHen')}
                className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-800 text-xs font-bold rounded-xl transition"
              >
                Về danh sách lịch khám
              </button>
            </div>
          ) : (
            <form onSubmit={handleHoanThanhKham} className="space-y-6">
              <div className="p-4 bg-sky-50 rounded-2xl border border-sky-100 flex justify-between items-center">
                <div>
                  <span className="text-xs text-slate-500">Đang khám cho bệnh nhân:</span>
                  <h3 className="text-base font-black text-sky-900">{selectedLich.tenBenhNhan} (Mã: {selectedLich.maDatLich})</h3>
                </div>
                <span className="text-xs font-mono font-bold text-sky-700">{selectedLich.gioBatDau?.slice(0, 5)} - {selectedLich.gioKetThuc?.slice(0, 5)}</span>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="sm:col-span-2">
                  <label className="block text-xs font-bold text-slate-700 mb-1">Triệu Chứng Ghi Nhận (*)</label>
                  <textarea
                    rows="2"
                    value={trieuChung}
                    onChange={(e) => setTrieuChung(e.target.value)}
                    className="w-full p-3 rounded-xl border border-slate-300 text-xs focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-sky-400"
                    required
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">Chẩn Đoán Bệnh (*)</label>
                  <input
                    type="text"
                    placeholder="Ví dụ: Viêm họng cấp, huyết áp cao độ 1..."
                    value={chanDoan}
                    onChange={(e) => setChanDoan(e.target.value)}
                    className="w-full p-3 rounded-xl border border-slate-300 text-xs focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-sky-400"
                    required
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">Lời Dặn Bác Sĩ</label>
                  <input
                    type="text"
                    placeholder="Ví dụ: Uống nhiều nước, tái khám sau 7 ngày..."
                    value={loiDan}
                    onChange={(e) => setLoiDan(e.target.value)}
                    className="w-full p-3 rounded-xl border border-slate-300 text-xs focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-sky-400"
                  />
                </div>
                <div className="sm:col-span-2">
                  <label className="block text-xs font-bold text-slate-700 mb-1">Ghi Chú Đơn Thuốc</label>
                  <input
                    type="text"
                    placeholder="Ví dụ: Uống sau ăn, tránh đồ lạnh..."
                    value={ghiChuThuoc}
                    onChange={(e) => setGhiChuThuoc(e.target.value)}
                    className="w-full p-3 rounded-xl border border-slate-300 text-xs focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-sky-400"
                  />
                </div>
              </div>

              {/* Kê đơn thuốc */}
              <div className="space-y-3 pt-4 border-t border-slate-100">
                <div className="flex justify-between items-center">
                  <h4 className="text-xs font-extrabold text-slate-800">Kê Đơn Thuốc Điều Trị</h4>
                  <button
                    type="button"
                    onClick={addThuocRow}
                    className="flex items-center gap-1 px-3 py-1.5 bg-sky-50 text-sky-700 hover:bg-sky-100 text-xs font-bold rounded-lg transition"
                  >
                    <Plus className="w-3.5 h-3.5" /> Thêm Thuốc
                  </button>
                </div>

                <div className="space-y-3">
                  {danhSachThuoc.map((item, idx) => (
                    <div key={idx} className="grid grid-cols-12 gap-2 p-3 bg-slate-50 rounded-xl items-center text-xs">
                      <div className="col-span-4">
                        <input
                          type="text"
                          placeholder="Tên thuốc..."
                          value={item.tenThuoc}
                          onChange={(e) => handleThuocChange(idx, 'tenThuoc', e.target.value)}
                          className="w-full p-2 bg-white rounded-lg border border-slate-200 focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-sky-400"
                        />
                      </div>
                      <div className="col-span-2">
                        <input
                          type="number"
                          placeholder="SL"
                          min="1"
                          value={item.soLuong}
                          onChange={(e) => handleThuocChange(idx, 'soLuong', Number(e.target.value))}
                          className="w-full p-2 bg-white rounded-lg border border-slate-200 focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-sky-400"
                        />
                      </div>
                      <div className="col-span-3">
                        <input
                          type="text"
                          placeholder="Liều dùng (VD: 1 viên/lần)"
                          value={item.lieuDung}
                          onChange={(e) => handleThuocChange(idx, 'lieuDung', e.target.value)}
                          className="w-full p-2 bg-white rounded-lg border border-slate-200 focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-sky-400"
                        />
                      </div>
                      <div className="col-span-2">
                        <input
                          type="text"
                          placeholder="Cách dùng"
                          value={item.cachDung}
                          onChange={(e) => handleThuocChange(idx, 'cachDung', e.target.value)}
                          className="w-full p-2 bg-white rounded-lg border border-slate-200 focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-sky-400"
                        />
                      </div>
                      <div className="col-span-1 text-center">
                        {danhSachThuoc.length > 1 && (
                          <button
                            type="button"
                            onClick={() => removeThuocRow(idx)}
                            className="p-1.5 text-rose-500 hover:bg-rose-100 rounded-lg transition"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              <div className="flex gap-3 pt-4 border-t border-slate-100">
                <button
                  type="submit"
                  disabled={submittingKham}
                  className="px-6 py-3 bg-emerald-500 hover:bg-emerald-600 text-white rounded-xl font-bold text-xs shadow-md transition disabled:opacity-50"
                >
                  {submittingKham ? 'Đang Xử Lý...' : 'Hoàn Tất Khám & Lưu Đơn Thuốc'}
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setSelectedLich(null);
                    setActiveTab('lichHen');
                  }}
                  className="px-4 py-3 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-xl font-bold text-xs transition"
                >
                  Quay lại
                </button>
              </div>
            </form>
          )}
        </div>
      )}
    </div>
  );
}