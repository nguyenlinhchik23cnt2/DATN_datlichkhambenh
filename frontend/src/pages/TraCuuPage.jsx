import React, { useEffect, useState } from 'react';

import api from '../api/client';
import { useAuth } from '../context/AuthContext';

export default function TraCuuPage() {
  const { user } = useAuth();

  const maBenhNhan = user?.maBenhNhan;

  const [lichHenList, setLichHenList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState('');

  // Lưu chi tiết từng lịch hẹn
  const [chiTietMap, setChiTietMap] = useState({});
  const [loadingDetail, setLoadingDetail] = useState(null);

  // Mã lịch hẹn đang được xóa
  const [deletingId, setDeletingId] = useState(null);

  // Lọc trạng thái lịch hẹn
  const [filterStatus, setFilterStatus] = useState('TatCa');

  // Tải lịch sử khám của bệnh nhân
  const loadHistory = async () => {
    if (!maBenhNhan) {
      setLoading(false);
      return;
    }

    setLoading(true);
    setErrorMsg('');

    try {
      const res = await api.get(
        `/lich-hen/benh-nhan/${maBenhNhan}/lich-su`
      );

      const data = Array.isArray(res.data) ? res.data : [];

      // Sắp xếp lịch mới nhất lên đầu
      data.sort((a, b) => {
        return new Date(b.ngayDat) - new Date(a.ngayDat);
      });

      setLichHenList(data);

    } catch (error) {
      console.error('Lỗi tải lịch sử khám:', error);

      setErrorMsg(
        error.response?.data?.message ||
        'Không thể tải lịch sử khám bệnh. Vui lòng thử lại!'
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadHistory();
  }, [maBenhNhan]);

  // Định dạng ngày
  const formatDate = (date) => {
    if (!date) return 'Chưa có thông tin';

    const value = String(date).split('T')[0];
    const parts = value.split('-');

    if (parts.length !== 3) return date;

    return `${parts[2]}/${parts[1]}/${parts[0]}`;
  };

  // Định dạng ngày giờ
  const formatDateTime = (date) => {
    if (!date) return 'Chưa có thông tin';

    return new Date(date).toLocaleString('vi-VN');
  };

  // Định dạng giờ
  const formatTime = (time) => {
    if (!time) return '--:--';
    return String(time).slice(0, 5);
  };

  // Lịch quá hạn: còn chờ duyệt / đã duyệt nhưng đã qua giờ kết thúc khám
  const isOverdue = (item) => {
    if (item.trangThai !== 'ChoDuyet' && item.trangThai !== 'DaDuyet') {
      return false;
    }

    if (!item.ngayKham || !item.gioKetThuc) return false;

    const [y, m, d] = String(item.ngayKham).split('T')[0].split('-').map(Number);
    const [h, min] = String(item.gioKetThuc).split(':').map(Number);

    if ([y, m, d, h, min].some(Number.isNaN)) return false;

    return new Date(y, m - 1, d, h, min) < new Date();
  };

  // Trạng thái dùng để hiển thị / lọc / thống kê
  const getDisplayStatus = (item) =>
    isOverdue(item) ? 'QuaHan' : item.trangThai;

  // Hiển thị trạng thái
  const renderStatus = (status) => {
    switch (status) {
      case 'ChoDuyet':
        return (
          <span className="inline-flex items-center px-3 py-1.5 rounded-full bg-amber-100 text-amber-800 text-xs font-bold">
            Chờ duyệt
          </span>
        );

      case 'DaDuyet':
        return (
          <span className="inline-flex items-center px-3 py-1.5 rounded-full bg-sky-100 text-sky-800 text-xs font-bold">
            Đã duyệt
          </span>
        );

      case 'HoanThanh':
        return (
          <span className="inline-flex items-center px-3 py-1.5 rounded-full bg-emerald-100 text-emerald-800 text-xs font-bold">
            Đã khám
          </span>
        );

      case 'DaHuy':
        return (
          <span className="inline-flex items-center px-3 py-1.5 rounded-full bg-rose-100 text-rose-800 text-xs font-bold">
            Đã hủy
          </span>
        );

      case 'QuaHan':
        return (
          <span className="inline-flex items-center px-3 py-1.5 rounded-full bg-orange-100 text-orange-800 text-xs font-bold">
            Đã quá hạn khám
          </span>
        );

      default:
        return (
          <span className="px-3 py-1.5 rounded-full bg-slate-100 text-slate-700 text-xs font-bold">
            {status || 'Không xác định'}
          </span>
        );
    }
  };

  // Lấy kết quả khám, đơn thuốc và thông tin bác sĩ
  const handleViewDetail = (lichHen) => {
    const id = lichHen.maLichHen;

    setChiTietMap((prev) => {
      // Đã tải rồi: chỉ đóng/mở
      if (prev[id]) {
        return { ...prev, [id]: { ...prev[id], open: !prev[id].open } };
      }

      const danhSachThuoc = Array.isArray(lichHen.danhSachThuoc)
        ? lichHen.danhSachThuoc
        : [];

      const ketQuaList = lichHen.maKetQua != null
        ? [{
            maKetQua: lichHen.maKetQua,
            ngayKham: lichHen.ngayKham,
            trieuChung: lichHen.trieuChung,
            chanDoan: lichHen.chanDoan,
            loiDanBacSi: lichHen.loiDanBacSi,
            donThuocList: lichHen.maDonThuoc != null
              ? [{
                  maDonThuoc: lichHen.maDonThuoc,
                  ngayKeDon: lichHen.ngayKham,
                  ghiChu: lichHen.ghiChuDonThuoc,
                  chiTietList: danhSachThuoc.map((t, i) => ({ maChiTiet: i, ...t }))
                }]
              : []
          }]
        : [];

      return {
        ...prev,
        [id]: {
          open: true,
          loaded: true,
          lichLamViec: { tenBacSi: lichHen.tenBacSi, ngayLamViec: lichHen.ngayKham },
          ketQuaList
        }
      };
    });
  };

  // Xóa lịch hẹn quá hạn
  const handleDelete = async (lichHen) => {
    const id = lichHen.maLichHen;

    const confirmed = window.confirm(
      `Bạn có chắc chắn muốn xóa lịch hẹn ${lichHen.maDatLich || `LH-${id}`}?\nHành động này không thể hoàn tác.`
    );

    if (!confirmed) return;

    try {
      setDeletingId(id);

      await api.delete(`/lich-hen/${id}`);

      // Bỏ khỏi danh sách ngay trên giao diện
      setLichHenList((prev) => prev.filter((x) => x.maLichHen !== id));

      setChiTietMap((prev) => {
        const next = { ...prev };
        delete next[id];
        return next;
      });
    } catch (error) {
      console.error('Lỗi xóa lịch hẹn:', error);

      alert(
        error.response?.data?.message ||
        'Xóa lịch hẹn thất bại. Vui lòng thử lại!'
      );
    } finally {
      setDeletingId(null);
    }
  };

  // Lọc lịch theo trạng thái
  const filteredList = lichHenList.filter(item => {
    if (filterStatus === 'TatCa') return true;
    return getDisplayStatus(item) === filterStatus;
  });

  // Thống kê (lịch quá hạn được tính riêng)
  const countBy = (status) =>
    lichHenList.filter(x => getDisplayStatus(x) === status).length;

  const total = lichHenList.length;
  const choDuyet = countBy('ChoDuyet');
  const daDuyet = countBy('DaDuyet');
  const hoanThanh = countBy('HoanThanh');
  const daHuy = countBy('DaHuy');
  const quaHan = countBy('QuaHan');

  // Chưa đăng nhập
  if (!user) {
    return (
      <div className="max-w-3xl mx-auto my-12 px-4">
        <div className="bg-white rounded-3xl border p-8 text-center shadow-sm">
          <h2 className="text-xl font-bold text-slate-800">
            Vui lòng đăng nhập
          </h2>

          <p className="text-slate-500 text-sm mt-2">
            Bạn cần đăng nhập tài khoản bệnh nhân để xem lịch sử khám bệnh.
          </p>

          <a
            href="/dang-nhap"
            className="inline-block mt-5 px-6 py-3 bg-emerald-500 text-white rounded-xl font-bold hover:bg-emerald-600"
          >
            Đăng nhập ngay
          </a>
        </div>
      </div>
    );
  }

  // Không phải tài khoản bệnh nhân
  if (user.vaiTro !== 'BenhNhan') {
    return (
      <div className="max-w-3xl mx-auto my-12 px-4">
        <div className="bg-amber-50 border border-amber-200 rounded-2xl p-6 text-center">
          <h2 className="font-bold text-slate-800">
            Chức năng dành cho bệnh nhân
          </h2>

          <p className="text-sm text-slate-600 mt-2">
            Tài khoản hiện tại không có quyền xem lịch sử khám bệnh của bệnh nhân.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto my-8 px-4 sm:px-6 space-y-6">

      {/* Tiêu đề */}
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <span className="block text-sm font-bold text-sky-700 mb-2">
            HỒ SƠ KHÁM BỆNH
          </span>

          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-800">
            Lịch sử khám bệnh
          </h1>

          <p className="text-slate-500 text-sm mt-2">
            Xin chào {user.hoTen || user.tenDangNhap},
            đây là danh sách lịch hẹn và hồ sơ khám bệnh của bạn.
          </p>
        </div>

        <button
          onClick={loadHistory}
          disabled={loading}
          className="inline-flex items-center px-4 py-2.5 bg-sky-500 text-white rounded-xl font-bold text-sm hover:bg-sky-600 disabled:opacity-50"
        >
          {loading ? 'Đang tải...' : 'Làm mới'}
        </button>
      </div>

      {/* Thống kê */}
      <div className="grid grid-cols-2 md:grid-cols-6 gap-3">

        <StatCard
          title="Tổng lịch đặt"
          value={total}
          color="slate"
        />

        <StatCard
          title="Chờ duyệt"
          value={choDuyet}
          color="amber"
        />

        <StatCard
          title="Đã duyệt"
          value={daDuyet}
          color="sky"
        />

        <StatCard
          title="Đã khám"
          value={hoanThanh}
          color="emerald"
        />

        <StatCard
          title="Quá hạn"
          value={quaHan}
          color="orange"
        />

        <StatCard
          title="Đã hủy"
          value={daHuy}
          color="rose"
        />

      </div>

      {/* Lọc trạng thái */}
      <div className="bg-white border border-slate-200 rounded-2xl p-4">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h2 className="font-bold text-slate-800">
              Danh sách lịch hẹn
            </h2>

            <p className="text-xs text-slate-500 mt-1">
              Chọn từng lịch để xem kết quả khám và đơn thuốc.
            </p>
          </div>

          <select
            value={filterStatus}
            onChange={e => setFilterStatus(e.target.value)}
            className="px-4 py-2.5 border border-slate-300 rounded-xl text-sm outline-none focus:ring-2 focus:ring-sky-300 focus:border-sky-400"
          >
            <option value="TatCa">Tất cả lịch hẹn</option>
            <option value="ChoDuyet">Chờ duyệt</option>
            <option value="DaDuyet">Đã duyệt</option>
            <option value="HoanThanh">Đã khám</option>
            <option value="QuaHan">Quá hạn khám</option>
            <option value="DaHuy">Đã hủy</option>
          </select>
        </div>
      </div>

      {/* Đang tải */}
      {loading && (
        <div className="bg-white rounded-2xl border p-12 text-center">
          <p className="text-sm text-slate-500">
            Đang tải lịch sử khám bệnh...
          </p>
        </div>
      )}

      {/* Lỗi */}
      {!loading && errorMsg && (
        <div className="bg-rose-50 border border-rose-200 rounded-2xl p-5 text-rose-700">
          <span className="text-sm">{errorMsg}</span>
        </div>
      )}

      {/* Không có lịch */}
      {!loading && !errorMsg && lichHenList.length === 0 && (
        <div className="bg-white rounded-3xl border border-slate-200 p-12 text-center">
          <h3 className="text-lg font-bold text-slate-700">
            Bạn chưa có lịch khám nào
          </h3>

          <p className="text-sm text-slate-500 mt-2">
            Các lịch hẹn bạn đã đặt sẽ được hiển thị tại đây.
          </p>

          <a
            href="/dat-lich"
            className="inline-block mt-5 px-6 py-3 bg-emerald-500 text-white rounded-xl font-bold hover:bg-emerald-600"
          >
            Đặt lịch khám
          </a>
        </div>
      )}

      {/* Danh sách lịch */}
      {!loading && !errorMsg && filteredList.length > 0 && (
        <div className="space-y-4">

          {filteredList.map((lichHen) => {
            const id = lichHen.maLichHen;
            const detail = chiTietMap[id];
            const isOpen = detail?.open;
            const isLoading = loadingDetail === id;
            const overdue = isOverdue(lichHen);
            const isDeleting = deletingId === id;

            return (
              <div
                key={id}
                className={`bg-white border rounded-3xl shadow-sm overflow-hidden ${
                  overdue ? 'border-orange-300' : 'border-slate-200'
                }`}
              >

                {/* Thông tin lịch hẹn */}
                <div className="p-5 sm:p-6">

                  <div className="flex flex-wrap items-start justify-between gap-3 mb-5">

                    <div>
                      <p className="text-xs text-slate-400 font-medium">
                        MÃ ĐẶT LỊCH
                      </p>

                      <h3 className="text-lg font-black text-sky-800 font-mono mt-1">
                        {lichHen.maDatLich || `LH-${id}`}
                      </h3>
                    </div>

                    {renderStatus(getDisplayStatus(lichHen))}

                  </div>

                  {/* Cảnh báo quá hạn */}
                  {overdue && (
                    <div className="mb-5 bg-orange-50 border border-orange-200 rounded-xl p-3 text-sm text-orange-800">
                      Lịch hẹn này đã quá hạn khám (ngày khám{' '}
                      <strong>{formatDate(lichHen.ngayKham)}</strong>,{' '}
                      {formatTime(lichHen.gioBatDau)} - {formatTime(lichHen.gioKetThuc)}).
                      Bạn có thể xóa lịch này và đặt lại lịch khám mới.
                    </div>
                  )}

                  <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">

                    <InfoItem
                      label="Bệnh nhân"
                      value={lichHen.tenBenhNhan || user.hoTen}
                    />

                    <InfoItem
                      label="Mã lịch làm việc"
                      value={lichHen.maLich}
                    />

                    <InfoItem
                      label="Khung giờ khám"
                      value={`${formatTime(lichHen.gioBatDau)} - ${formatTime(lichHen.gioKetThuc)}`}
                    />

                    <InfoItem
                      label="Ngày khám"
                      value={formatDate(lichHen.ngayKham)}
                    />

                    <InfoItem
                      label="Ngày đặt lịch"
                      value={formatDateTime(lichHen.ngayDat)}
                    />

                    <InfoItem
                      label="Lý do khám"
                      value={lichHen.lyDoKham || 'Chưa cập nhật'}
                    />

                    <InfoItem
                      label="Ghi chú"
                      value={lichHen.ghiChu || 'Không có ghi chú'}
                    />

                  </div>

                  {/* Nút thao tác */}
                  <div className="mt-5 pt-4 border-t border-slate-100 flex flex-wrap justify-end gap-3">

                    {overdue && (
                      <>
                        <a
                          href="/dat-lich"
                          className="inline-flex items-center px-5 py-2.5 bg-emerald-50 text-emerald-700 rounded-xl font-bold text-sm hover:bg-emerald-100"
                        >
                          Đặt lịch mới
                        </a>

                        <button
                          onClick={() => handleDelete(lichHen)}
                          disabled={isDeleting}
                          className="inline-flex items-center px-5 py-2.5 bg-rose-500 text-white rounded-xl font-bold text-sm hover:bg-rose-600 disabled:opacity-50"
                        >
                          {isDeleting ? 'Đang xóa...' : 'Xóa lịch'}
                        </button>
                      </>
                    )}

                    <button
                      onClick={() => handleViewDetail(lichHen)}
                      disabled={isLoading}
                      className="inline-flex items-center px-5 py-2.5 bg-sky-50 text-sky-700 rounded-xl font-bold text-sm hover:bg-sky-100 disabled:opacity-50"
                    >
                      {isLoading
                        ? 'Đang tải...'
                        : isOpen
                          ? 'Đóng chi tiết'
                          : 'Xem chi tiết'}
                    </button>

                  </div>

                </div>

                {/* Phần chi tiết */}
                {isOpen && (
                  <div className="bg-slate-50 border-t border-slate-200 p-5 sm:p-6 space-y-5">

                    {detail.error && (
                      <div className="text-sm text-rose-600 bg-rose-50 p-3 rounded-xl">
                        {detail.error}
                      </div>
                    )}

                    {/* Thông tin bác sĩ */}
                    <div className="bg-white border border-slate-200 rounded-2xl p-4">

                      <h4 className="font-bold text-sky-700 mb-3">
                        Thông tin bác sĩ và lịch khám
                      </h4>

                      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-sm">

                        <p>
                          <span className="text-slate-500">Bác sĩ:</span>{' '}
                          <strong className="text-slate-800">
                            {detail.lichLamViec?.tenBacSi || 'Chưa cập nhật'}
                          </strong>
                        </p>

                        <p>
                          <span className="text-slate-500">Ngày khám:</span>{' '}
                          <strong className="text-slate-800">
                            {formatDate(detail.lichLamViec?.ngayLamViec)}
                          </strong>
                        </p>

                        <p>
                          <span className="text-slate-500">Giờ bắt đầu:</span>{' '}
                          <strong className="text-slate-800">
                            {formatTime(lichHen.gioBatDau)}
                          </strong>
                        </p>

                        <p>
                          <span className="text-slate-500">Giờ kết thúc:</span>{' '}
                          <strong className="text-slate-800">
                            {formatTime(lichHen.gioKetThuc)}
                          </strong>
                        </p>

                      </div>
                    </div>

                    {/* Kết quả khám */}
                    <div>
                      <h4 className="font-bold text-slate-800 mb-3">
                        Kết quả khám bệnh
                      </h4>

                      {detail.ketQuaList?.length > 0 ? (
                        <div className="space-y-4">

                          {detail.ketQuaList.map((ketQua) => (
                            <div
                              key={ketQua.maKetQua}
                              className="bg-white border border-slate-200 rounded-2xl p-4 space-y-4"
                            >

                              <div className="flex flex-wrap justify-between gap-2">
                                <span className="font-bold text-sky-800">
                                  Kết quả khám #{ketQua.maKetQua}
                                </span>

                                <span className="text-xs text-slate-500">
                                  {formatDateTime(ketQua.ngayKham)}
                                </span>
                              </div>

                              <div>
                                <p className="text-xs font-bold text-slate-500 mb-1">
                                  TRIỆU CHỨNG
                                </p>

                                <p className="text-sm text-slate-700 whitespace-pre-line">
                                  {ketQua.trieuChung || 'Chưa cập nhật'}
                                </p>
                              </div>

                              <div>
                                <p className="text-xs font-bold text-slate-500 mb-1">
                                  CHẨN ĐOÁN
                                </p>

                                <p className="text-sm text-slate-800 font-medium whitespace-pre-line">
                                  {ketQua.chanDoan || 'Chưa cập nhật'}
                                </p>
                              </div>

                              <div className="bg-sky-50 rounded-xl p-3">
                                <p className="text-xs font-bold text-sky-700 mb-1">
                                  LỜI DẶN CỦA BÁC SĨ
                                </p>

                                <p className="text-sm text-sky-900 whitespace-pre-line">
                                  {ketQua.loiDanBacSi || 'Không có lời dặn'}
                                </p>
                              </div>

                              {/* Đơn thuốc */}
                              <div className="pt-3 border-t border-slate-100">

                                <h5 className="font-bold text-slate-800 mb-3">
                                  Đơn thuốc
                                </h5>

                                {ketQua.donThuocList?.length > 0 ? (
                                  <div className="space-y-3">

                                    {ketQua.donThuocList.map((donThuoc) => (
                                      <div
                                        key={donThuoc.maDonThuoc}
                                        className="border border-emerald-100 bg-emerald-50/50 rounded-xl p-3"
                                      >

                                        <div className="flex flex-wrap justify-between gap-2 mb-3">
                                          <span className="font-bold text-emerald-800 text-sm">
                                            Đơn thuốc #{donThuoc.maDonThuoc}
                                          </span>

                                          <span className="text-xs text-slate-500">
                                            {formatDateTime(donThuoc.ngayKeDon)}
                                          </span>
                                        </div>

                                        {donThuoc.ghiChu && (
                                          <p className="text-sm text-slate-700 mb-3">
                                            <strong>Ghi chú:</strong>{' '}
                                            {donThuoc.ghiChu}
                                          </p>
                                        )}

                                        {donThuoc.chiTietList?.length > 0 ? (
                                          <div className="space-y-3">

                                            {donThuoc.chiTietList.map((thuoc) => (
                                              <div
                                                key={thuoc.maChiTiet}
                                                className="bg-white rounded-xl border border-emerald-100 p-3"
                                              >

                                                <div className="flex flex-wrap justify-between gap-2">
                                                  <p className="font-bold text-slate-800 text-sm">
                                                    {thuoc.tenThuoc}
                                                  </p>

                                                  <span className="text-xs font-bold text-emerald-700">
                                                    Số lượng: {thuoc.soLuong}
                                                  </span>
                                                </div>

                                                <div className="mt-2 space-y-1 text-sm text-slate-600">
                                                  <p>
                                                    <strong>Liều dùng:</strong>{' '}
                                                    {thuoc.lieuDung || 'Chưa cập nhật'}
                                                  </p>

                                                  <p>
                                                    <strong>Cách dùng:</strong>{' '}
                                                    {thuoc.cachDung || 'Chưa cập nhật'}
                                                  </p>
                                                </div>

                                              </div>
                                            ))}

                                          </div>
                                        ) : (
                                          <p className="text-xs text-slate-500">
                                            Đơn thuốc chưa có chi tiết thuốc.
                                          </p>
                                        )}

                                      </div>
                                    ))}

                                  </div>
                                ) : (
                                  <p className="text-sm text-slate-500 bg-slate-50 rounded-xl p-3">
                                    Chưa có đơn thuốc cho kết quả khám này.
                                  </p>
                                )}

                              </div>

                            </div>
                          ))}

                        </div>
                      ) : (
                        <div className="bg-white border border-slate-200 rounded-xl p-5 text-center">
                          <p className="text-sm text-slate-500">
                            Chưa có kết quả khám.
                          </p>

                          <p className="text-xs text-slate-400 mt-1">
                            {overdue
                              ? 'Lịch hẹn đã quá hạn nên không có kết quả khám.'
                              : 'Kết quả sẽ hiển thị sau khi bác sĩ hoàn thành khám bệnh.'}
                          </p>
                        </div>
                      )}

                    </div>

                  </div>
                )}

              </div>
            );
          })}

        </div>
      )}

      {/* Không có kết quả lọc */}
      {!loading &&
        !errorMsg &&
        lichHenList.length > 0 &&
        filteredList.length === 0 && (
          <div className="bg-white border rounded-2xl p-8 text-center">
            <p className="font-bold text-slate-700">
              Không có lịch hẹn thuộc trạng thái này.
            </p>
          </div>
        )}

    </div>
  );
}

// Component thông tin
function InfoItem({ label, value }) {
  return (
    <div className="min-w-0">
      <p className="text-xs text-slate-400 mb-1">
        {label}
      </p>

      <p className="text-sm font-semibold text-slate-700 break-words">
        {value || 'Chưa cập nhật'}
      </p>
    </div>
  );
}

// Component thống kê
function StatCard({ title, value, color }) {
  const colorClass = {
    slate: 'text-slate-800',
    amber: 'text-amber-600',
    sky: 'text-sky-600',
    emerald: 'text-emerald-600',
    orange: 'text-orange-600',
    rose: 'text-rose-600'
  };

  return (
    <div className="bg-white border border-slate-200 rounded-2xl p-4">
      <p className="text-xs text-slate-500">
        {title}
      </p>

      <p className={`text-2xl font-black mt-2 ${colorClass[color]}`}>
        {value}
      </p>
    </div>
  );
}