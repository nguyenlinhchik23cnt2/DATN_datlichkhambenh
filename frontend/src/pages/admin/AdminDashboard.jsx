import React, { useState, useEffect, useMemo } from 'react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';
import api from '../../api/client';
import { useAuth } from '../../context/AuthContext';

/* ===================== Thành phần dùng chung ===================== */
const inputCls =
  'w-full p-2.5 rounded-xl border border-slate-300 text-xs focus:outline-none focus:ring-2 focus:ring-cyan-500';

function Field({ label, children }) {
  return (
    <div>
      <label className="block font-bold text-slate-700 mb-1 text-xs">{label}</label>
      {children}
    </div>
  );
}

function Badge({ active }) {
  return (
    <span className={`px-2.5 py-1 rounded-full text-[11px] font-bold ${
      active ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-500'}`}>
      {active ? 'Hoạt động' : 'Tạm ngưng'}
    </span>
  );
}

function Modal({ title, onClose, children }) {
  return (
    <div className="fixed inset-0 z-50 bg-slate-900/40 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-white max-w-lg w-full max-h-[90vh] overflow-y-auto rounded-3xl shadow-2xl border border-slate-200 p-6 sm:p-8 space-y-5">
        <div className="flex justify-between items-center pb-3 border-b border-slate-100">
          <h3 className="text-base font-extrabold text-slate-800">{title}</h3>
          <button onClick={onClose} className="px-3 py-1.5 rounded-lg text-xs font-bold text-slate-500 hover:bg-slate-100 hover:text-slate-700 transition">
            Đóng
          </button>
        </div>
        {children}
      </div>
    </div>
  );
}

function ModalActions({ onClose, saving, label }) {
  return (
    <div className="flex justify-end gap-2 pt-3 border-t border-slate-100">
      <button type="button" onClick={onClose}
        className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-xl text-xs font-bold transition">
        Hủy
      </button>
      <button type="submit" disabled={saving}
        className="px-5 py-2 bg-cyan-600 hover:bg-cyan-700 text-white rounded-xl text-xs font-bold transition disabled:opacity-50">
        {saving ? 'Đang lưu...' : label}
      </button>
    </div>
  );
}

/* ===================== Tab: Tổng quan ===================== */
function TabTongQuan() {
  const [tongQuan, setTongQuan] = useState(null);
  const [dataThang, setDataThang] = useState([]);
  const [dataChuyenKhoa, setDataChuyenKhoa] = useState([]);

  useEffect(() => {
    (async () => {
      try {
        const [tq, thang, ck] = await Promise.all([
          api.get('/thong-ke/tong-quan'),
          api.get('/thong-ke/theo-thang'),
          api.get('/thong-ke/theo-chuyen-khoa'),
        ]);
        setTongQuan(tq.data);
        setDataThang((thang.data || []).map(d => ({ ...d, name: `T${d.thang}` })));
        setDataChuyenKhoa(ck.data || []);
      } catch (err) {
        console.error(err);
      }
    })();
  }, []);

  // Không dùng icon: màu được áp cho con số để vẫn phân biệt được từng thẻ
  const stats = [
    { label: 'Tổng Bác Sĩ', value: tongQuan?.tongBacSi, color: 'text-cyan-700' },
    { label: 'Tổng Bệnh Nhân', value: tongQuan?.tongBenhNhan, color: 'text-blue-700' },
    { label: 'Lịch Đã Khám', value: tongQuan?.lichHenHoanThanh, color: 'text-purple-700' },
    { label: 'Đang Chờ Duyệt', value: tongQuan?.lichHenChoDuyet, color: 'text-amber-700' },
  ];

  return (
    <div className="space-y-8">
      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        {stats.map(({ label, value, color }) => (
          <div key={label} className="p-6 bg-white rounded-3xl border border-slate-200 shadow-sm">
            <span className="text-xs text-slate-500 font-medium">{label}</span>
            <h3 className={`text-2xl font-black ${color}`}>{value || 0}</h3>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white p-6 rounded-3xl border border-slate-200 shadow-sm space-y-4">
          <h3 className="text-sm font-extrabold text-slate-800">
            Số Lượng Lượt Khám Hoàn Thành Theo 12 Tháng
          </h3>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={dataThang}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                <XAxis dataKey="name" fontSize={11} stroke="#94a3b8" />
                <YAxis fontSize={11} stroke="#94a3b8" />
                <Tooltip />
                <Bar dataKey="soLuotKham" fill="#0891b2" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="bg-white p-6 rounded-3xl border border-slate-200 shadow-sm space-y-4">
          <h3 className="text-sm font-extrabold text-slate-800">
            Lượt Khám Phân Bổ Theo Chuyên Khoa
          </h3>
          <div className="h-64 overflow-y-auto space-y-3 pr-2">
            {dataChuyenKhoa.map((ck) => (
              <div key={ck.maChuyenKhoa} className="p-3 rounded-2xl bg-slate-50 border border-slate-100 flex justify-between items-center text-xs">
                <div>
                  <span className="font-bold text-slate-800">{ck.tenChuyenKhoa}</span>
                  <span className="block text-[10px] text-slate-400">Mã CK: #{ck.maChuyenKhoa}</span>
                </div>
                <span className="px-3 py-1 bg-purple-100 text-purple-700 font-extrabold rounded-full">
                  {ck.soLuotKham} lượt khám
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

/* ===================== Tab: Quản lý chuyên khoa ===================== */
const emptyCk = { maChuyenKhoa: null, tenChuyenKhoa: '', moTa: '', trangThai: true };

function TabChuyenKhoa() {
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [keyword, setKeyword] = useState('');
  const [form, setForm] = useState(null);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const load = async () => {
    try {
      const res = await api.get('/chuyen-khoa');
      setList(res.data || []);
    } catch (err) {
      setError('Không tải được danh sách chuyên khoa: ' + err.message);
    } finally {
      setLoading(false);
    }
  };
  useEffect(() => { load(); }, []);

  const filtered = useMemo(
    () => list.filter(c => c.tenChuyenKhoa.toLowerCase().includes(keyword.toLowerCase())),
    [list, keyword]
  );

  const save = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      const body = { tenChuyenKhoa: form.tenChuyenKhoa.trim(), moTa: form.moTa, trangThai: form.trangThai };
      if (form.maChuyenKhoa) await api.put(`/chuyen-khoa/${form.maChuyenKhoa}`, body);
      else await api.post('/chuyen-khoa', body);
      setForm(null);
      load();
    } catch (err) {
      setError('Lưu thất bại: ' + (err.response?.data?.message || err.message));
    } finally {
      setSaving(false);
    }
  };

  const remove = async (ck) => {
    if (!window.confirm(`Xóa chuyên khoa "${ck.tenChuyenKhoa}"?`)) return;
    try {
      await api.delete(`/chuyen-khoa/${ck.maChuyenKhoa}`);
      load();
    } catch {
      setError('Không thể xóa. Chuyên khoa có thể đang được bác sĩ sử dụng — hãy chuyển sang "Tạm ngưng" thay vì xóa.');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <p className="text-xs text-slate-500">{list.length} chuyên khoa</p>
        <div className="flex gap-3">
          <input value={keyword} onChange={(e) => setKeyword(e.target.value)}
            placeholder="Tìm theo tên" className={inputCls + ' w-56'} />
          <button onClick={() => { setError(''); setForm(emptyCk); }}
            className="px-4 py-2.5 bg-cyan-600 hover:bg-cyan-700 text-white rounded-xl text-xs font-bold transition shadow-sm">
            Thêm chuyên khoa
          </button>
        </div>
      </div>

      {error && !form && (
        <div className="p-3 rounded-2xl bg-red-50 border border-red-200 text-xs text-red-700">{error}</div>
      )}

      <div className="bg-white rounded-3xl border border-slate-200 shadow-sm overflow-x-auto">
        <table className="w-full text-xs text-left">
          <thead className="bg-slate-50 text-slate-500">
            <tr>
              <th className="px-5 py-3 font-bold w-20">Mã</th>
              <th className="px-5 py-3 font-bold">Tên chuyên khoa</th>
              <th className="px-5 py-3 font-bold">Mô tả</th>
              <th className="px-5 py-3 font-bold">Trạng thái</th>
              <th className="px-5 py-3 font-bold text-right">Thao tác</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {loading && <tr><td colSpan={5} className="px-5 py-8 text-center text-slate-400">Đang tải...</td></tr>}
            {!loading && filtered.length === 0 && (
              <tr><td colSpan={5} className="px-5 py-8 text-center text-slate-400">Chưa có chuyên khoa nào. Chọn "Thêm chuyên khoa" để tạo mới.</td></tr>
            )}
            {filtered.map((ck) => (
              <tr key={ck.maChuyenKhoa} className="hover:bg-slate-50">
                <td className="px-5 py-3 text-slate-400">#{ck.maChuyenKhoa}</td>
                <td className="px-5 py-3 font-bold text-slate-800">{ck.tenChuyenKhoa}</td>
                <td className="px-5 py-3 text-slate-600 max-w-xs truncate">{ck.moTa}</td>
                <td className="px-5 py-3"><Badge active={ck.trangThai} /></td>
                <td className="px-5 py-3">
                  <div className="flex justify-end gap-1">
                    <button className="px-3 py-1.5 rounded-lg text-xs font-bold text-cyan-700 hover:bg-cyan-50"
                      onClick={() => { setError(''); setForm({ ...emptyCk, ...ck, moTa: ck.moTa || '' }); }}>
                      Sửa
                    </button>
                    <button onClick={() => remove(ck)} className="px-3 py-1.5 rounded-lg text-xs font-bold text-red-600 hover:bg-red-50">
                      Xóa
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {form && (
        <Modal title={form.maChuyenKhoa ? 'Sửa chuyên khoa' : 'Thêm chuyên khoa'} onClose={() => setForm(null)}>
          <form onSubmit={save} className="space-y-4">
            <Field label="Tên chuyên khoa (*)">
              <input className={inputCls} required maxLength={100} value={form.tenChuyenKhoa}
                onChange={(e) => setForm({ ...form, tenChuyenKhoa: e.target.value })} />
            </Field>
            <Field label="Mô tả">
              <textarea className={inputCls} rows={3} maxLength={500} value={form.moTa}
                onChange={(e) => setForm({ ...form, moTa: e.target.value })} />
            </Field>
            <Field label="Trạng thái">
              <select className={inputCls + ' bg-white'} value={String(form.trangThai)}
                onChange={(e) => setForm({ ...form, trangThai: e.target.value === 'true' })}>
                <option value="true">Hoạt động</option>
                <option value="false">Tạm ngưng</option>
              </select>
            </Field>
            {error && <p className="text-xs text-red-600">{error}</p>}
            <ModalActions onClose={() => setForm(null)} saving={saving} label="Lưu chuyên khoa" />
          </form>
        </Modal>
      )}
    </div>
  );
}

/* ===================== Tab: Quản lý bác sĩ ===================== */
const emptyBs = {
  maBacSi: null, maTaiKhoan: null,
  tenDangNhap: '', matKhau: '', hoTen: '', email: '', soDienThoai: '',
  maChuyenKhoa: '', hocVi: 'Thạc sĩ - Bác sĩ', kinhNghiem: '', gioiThieu: '', trangThai: true,
};

function TabBacSi() {
  const [list, setList] = useState([]);
  const [chuyenKhoas, setChuyenKhoas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [keyword, setKeyword] = useState('');
  const [filterCk, setFilterCk] = useState('');
  const [form, setForm] = useState(null);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const load = async () => {
    try {
      const [bs, ck] = await Promise.all([api.get('/bac-si'), api.get('/chuyen-khoa')]);
      setList(bs.data || []);
      setChuyenKhoas(ck.data || []);
    } catch (err) {
      setError('Không tải được dữ liệu: ' + err.message);
    } finally {
      setLoading(false);
    }
  };
  useEffect(() => { load(); }, []);

  const tenCk = (id) => chuyenKhoas.find(c => c.maChuyenKhoa === id)?.tenChuyenKhoa || '—';

  const filtered = useMemo(
    () => list.filter(b =>
      (b.tenBacSi || '').toLowerCase().includes(keyword.toLowerCase()) &&
      (!filterCk || b.maChuyenKhoa === Number(filterCk))),
    [list, keyword, filterCk]
  );

  const isEdit = !!form?.maBacSi;
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const save = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      if (isEdit) {
        // PUT gửi cả entity nên phải giữ nguyên maTaiKhoan
        await api.put(`/bac-si/${form.maBacSi}`, {
          maTaiKhoan: form.maTaiKhoan,
          maChuyenKhoa: Number(form.maChuyenKhoa),
          tenBacSi: form.hoTen,
          hocVi: form.hocVi,
          kinhNghiem: form.kinhNghiem,
          gioiThieu: form.gioiThieu,
          trangThai: form.trangThai,
        });
      } else {
        await api.post('/bac-si/tao-moi', {
          tenDangNhap: form.tenDangNhap, matKhau: form.matKhau, hoTen: form.hoTen,
          email: form.email, soDienThoai: form.soDienThoai,
          maChuyenKhoa: Number(form.maChuyenKhoa),
          hocVi: form.hocVi, kinhNghiem: form.kinhNghiem, gioiThieu: form.gioiThieu,
        });
      }
      setForm(null);
      load();
    } catch (err) {
      setError('Lưu thất bại: ' + (err.response?.data?.message || err.message));
    } finally {
      setSaving(false);
    }
  };

  const remove = async (b) => {
    if (!window.confirm(`Xóa hồ sơ bác sĩ "${b.tenBacSi}"?`)) return;
    try {
      await api.delete(`/bac-si/${b.maBacSi}`);
      load();
    } catch {
      setError('Không thể xóa. Bác sĩ có thể đã có lịch hẹn — hãy chuyển sang "Tạm ngưng" thay vì xóa.');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <p className="text-xs text-slate-500">{list.length} bác sĩ</p>
        <div className="flex flex-wrap gap-3">
          <input value={keyword} onChange={(e) => setKeyword(e.target.value)}
            placeholder="Tìm theo tên" className={inputCls + ' w-48'} />
          <select value={filterCk} onChange={(e) => setFilterCk(e.target.value)} className={inputCls + ' bg-white w-48'}>
            <option value="">Tất cả chuyên khoa</option>
            {chuyenKhoas.map(c => <option key={c.maChuyenKhoa} value={c.maChuyenKhoa}>{c.tenChuyenKhoa}</option>)}
          </select>
          <button onClick={() => { setError(''); setForm(emptyBs); }}
            className="px-4 py-2.5 bg-cyan-600 hover:bg-cyan-700 text-white rounded-xl text-xs font-bold transition shadow-sm">
            Thêm bác sĩ
          </button>
        </div>
      </div>

      {error && !form && (
        <div className="p-3 rounded-2xl bg-red-50 border border-red-200 text-xs text-red-700">{error}</div>
      )}

      <div className="bg-white rounded-3xl border border-slate-200 shadow-sm overflow-x-auto">
        <table className="w-full text-xs text-left">
          <thead className="bg-slate-50 text-slate-500">
            <tr>
              <th className="px-5 py-3 font-bold w-20">Mã</th>
              <th className="px-5 py-3 font-bold">Bác sĩ</th>
              <th className="px-5 py-3 font-bold">Chuyên khoa</th>
              <th className="px-5 py-3 font-bold">Học vị</th>
              <th className="px-5 py-3 font-bold">Trạng thái</th>
              <th className="px-5 py-3 font-bold text-right">Thao tác</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {loading && <tr><td colSpan={6} className="px-5 py-8 text-center text-slate-400">Đang tải...</td></tr>}
            {!loading && filtered.length === 0 && (
              <tr><td colSpan={6} className="px-5 py-8 text-center text-slate-400">Không có bác sĩ nào phù hợp.</td></tr>
            )}
            {filtered.map((b) => (
              <tr key={b.maBacSi} className="hover:bg-slate-50">
                <td className="px-5 py-3 text-slate-400">#{b.maBacSi}</td>
                <td className="px-5 py-3 font-bold text-slate-800">{b.tenBacSi}</td>
                <td className="px-5 py-3 text-slate-600">{tenCk(b.maChuyenKhoa)}</td>
                <td className="px-5 py-3 text-slate-600">{b.hocVi}</td>
                <td className="px-5 py-3"><Badge active={b.trangThai} /></td>
                <td className="px-5 py-3">
                  <div className="flex justify-end gap-1">
                    <button className="px-3 py-1.5 rounded-lg text-xs font-bold text-cyan-700 hover:bg-cyan-50"
                      onClick={() => {
                        setError('');
                        setForm({
                          ...emptyBs, maBacSi: b.maBacSi, maTaiKhoan: b.maTaiKhoan,
                          hoTen: b.tenBacSi || '', maChuyenKhoa: b.maChuyenKhoa,
                          hocVi: b.hocVi || '', kinhNghiem: b.kinhNghiem || '',
                          gioiThieu: b.gioiThieu || '', trangThai: b.trangThai,
                        });
                      }}>
                      Sửa
                    </button>
                    <button onClick={() => remove(b)} className="px-3 py-1.5 rounded-lg text-xs font-bold text-red-600 hover:bg-red-50">
                      Xóa
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {form && (
        <Modal title={isEdit ? 'Sửa hồ sơ bác sĩ' : 'Thêm bác sĩ mới'} onClose={() => setForm(null)}>
          <form onSubmit={save} className="space-y-4">
            {!isEdit && (
              <div className="grid grid-cols-2 gap-3">
                <Field label="Tên đăng nhập (*)">
                  <input className={inputCls} required maxLength={50} value={form.tenDangNhap} onChange={set('tenDangNhap')} />
                </Field>
                <Field label="Mật khẩu (*)">
                  <input type="password" className={inputCls} required minLength={6} maxLength={100}
                    value={form.matKhau} onChange={set('matKhau')} />
                </Field>
              </div>
            )}
            <Field label="Họ và tên bác sĩ (*)">
              <input className={inputCls} required maxLength={100} value={form.hoTen} onChange={set('hoTen')} />
            </Field>
            <div className="grid grid-cols-2 gap-3">
              <Field label="Chuyên khoa (*)">
                <select className={inputCls + ' bg-white'} required value={form.maChuyenKhoa} onChange={set('maChuyenKhoa')}>
                  <option value="">-- Chọn chuyên khoa --</option>
                  {chuyenKhoas.filter(c => c.trangThai || c.maChuyenKhoa === form.maChuyenKhoa).map(c => (
                    <option key={c.maChuyenKhoa} value={c.maChuyenKhoa}>{c.tenChuyenKhoa}</option>
                  ))}
                </select>
              </Field>
              <Field label="Học vị">
                <input className={inputCls} maxLength={50} value={form.hocVi} onChange={set('hocVi')} />
              </Field>
            </div>
            {!isEdit && (
              <div className="grid grid-cols-2 gap-3">
                <Field label="Email">
                  <input type="email" className={inputCls} maxLength={100} value={form.email} onChange={set('email')} />
                </Field>
                <Field label="Số điện thoại">
                  <input className={inputCls} maxLength={15} value={form.soDienThoai} onChange={set('soDienThoai')} />
                </Field>
              </div>
            )}
            <Field label="Kinh nghiệm">
              <textarea rows={2} className={inputCls} maxLength={255} value={form.kinhNghiem} onChange={set('kinhNghiem')} />
            </Field>
            <Field label="Giới thiệu">
              <textarea rows={3} className={inputCls} maxLength={500} value={form.gioiThieu} onChange={set('gioiThieu')} />
            </Field>
            {isEdit && (
              <Field label="Trạng thái">
                <select className={inputCls + ' bg-white'} value={String(form.trangThai)}
                  onChange={(e) => setForm({ ...form, trangThai: e.target.value === 'true' })}>
                  <option value="true">Hoạt động</option>
                  <option value="false">Tạm ngưng</option>
                </select>
              </Field>
            )}
            {error && <p className="text-xs text-red-600">{error}</p>}
            <ModalActions onClose={() => setForm(null)} saving={saving} label={isEdit ? 'Lưu thay đổi' : 'Tạo bác sĩ'} />
          </form>
        </Modal>
      )}
    </div>
  );
}

/* ===================== AdminDashboard (sidebar + nội dung) ===================== */
const MENU = [
  { key: 'tong-quan', label: 'Tổng quan' },
  { key: 'bac-si', label: 'Quản lý bác sĩ' },
  { key: 'chuyen-khoa', label: 'Quản lý chuyên khoa' },
];

export default function AdminDashboard() {
  const { user } = useAuth();
  const [tab, setTab] = useState('tong-quan');
  const current = MENU.find(m => m.key === tab);

  return (
    <div className="min-h-screen flex flex-col md:flex-row bg-slate-50">
      {/* Sidebar */}
      <aside className="md:w-64 md:min-h-screen bg-white border-b md:border-b-0 md:border-r border-slate-200 p-4 md:p-5 md:sticky md:top-0 md:self-start">
        <div className="mb-4 md:mb-8">
          <p className="text-sm font-extrabold text-slate-800">Quản Trị Hệ Thống</p>
          <p className="text-[11px] text-slate-500">Xin chào, {user?.hoTen}</p>
        </div>
        <nav className="flex md:flex-col gap-1 overflow-x-auto">
          {MENU.map(({ key, label }) => (
            <button
              key={key}
              onClick={() => setTab(key)}
              className={`px-4 py-2.5 rounded-xl text-xs font-bold whitespace-nowrap transition text-left ${
                tab === key ? 'bg-cyan-600 text-white shadow-sm' : 'text-slate-600 hover:bg-slate-100'
              }`}
            >
              {label}
            </button>
          ))}
        </nav>
      </aside>

      {/* Nội dung */}
      <main className="flex-1 min-w-0 p-4 sm:p-6 lg:p-8 space-y-6">
        <h1 className="text-xl font-extrabold text-slate-800">{current.label}</h1>
        {tab === 'tong-quan' && <TabTongQuan />}
        {tab === 'bac-si' && <TabBacSi />}
        {tab === 'chuyen-khoa' && <TabChuyenKhoa />}
      </main>
    </div>
  );
}