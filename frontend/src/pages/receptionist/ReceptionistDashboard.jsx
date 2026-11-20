import React, { useEffect, useMemo, useState } from 'react';
import api from '../../api/client';
import { useAuth } from '../../context/AuthContext';

const DAY_NAMES = ['Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7', 'Chủ nhật'];

// Ngày khám: ưu tiên ngày làm việc của lịch (nếu backend trả về), không có thì dùng ngày đặt
const getAppointmentDate = (item) =>
  item.ngayKham || item.ngayLamViec || item.ngayDat;

// Parse an toàn, tránh lệch múi giờ với chuỗi 'YYYY-MM-DD'
const parseDate = (value) => {
  if (!value) return null;

  const m = /^(\d{4})-(\d{2})-(\d{2})$/.exec(String(value));
  const d = m ? new Date(+m[1], +m[2] - 1, +m[3]) : new Date(value);

  if (Number.isNaN(d.getTime())) return null;

  d.setHours(0, 0, 0, 0);
  return d;
};

const formatDate = (d) =>
  d.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  });

const isToday = (d) => d.toDateString() === new Date().toDateString();

export default function ReceptionistDashboard() {
  const { user, logout } = useAuth();

  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [keyword, setKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState('TatCa');

  // Lấy danh sách lịch hẹn
  const fetchAppointments = async () => {
    try {
      setLoading(true);
      setError('');

      const response = await api.get('/lich-hen');

      setAppointments(
        Array.isArray(response.data) ? response.data : []
      );
    } catch (err) {
      console.error('Lỗi lấy danh sách lịch hẹn:', err);

      setError(
        err.response?.data?.message ||
        'Không thể tải danh sách lịch hẹn!'
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAppointments();
  }, []);

  // Thứ 2 của tuần hiện tại
  const weekStart = useMemo(() => {
    const d = new Date();
    const day = d.getDay(); // 0 = Chủ nhật

    d.setDate(d.getDate() + (day === 0 ? -6 : 1 - day));
    d.setHours(0, 0, 0, 0);

    return d;
  }, []);

  // Chủ nhật của tuần hiện tại
  const weekEnd = useMemo(() => {
    const d = new Date(weekStart);
    d.setDate(d.getDate() + 6);
    return d;
  }, [weekStart]);

  // Chỉ giữ lịch hẹn nằm trong tuần này (lịch ngoài tuần bị ẩn khỏi màn hình)
  const weekAppointments = useMemo(() => {
    return appointments.filter(item => {
      const d = parseDate(getAppointmentDate(item));
      return d && d >= weekStart && d <= weekEnd;
    });
  }, [appointments, weekStart, weekEnd]);

  // Thống kê lịch hẹn trong tuần
  const statistics = useMemo(() => {
    return {
      total: weekAppointments.length,

      waiting: weekAppointments.filter(
        item => item.trangThai === 'ChoDuyet'
      ).length,

      approved: weekAppointments.filter(
        item => item.trangThai === 'DaDuyet'
      ).length,

      completed: weekAppointments.filter(
        item => item.trangThai === 'HoanThanh'
      ).length,

      cancelled: weekAppointments.filter(
        item => item.trangThai === 'DaHuy'
      ).length
    };
  }, [weekAppointments]);

  // Tìm kiếm và lọc (trên lịch của tuần này)
  const filteredAppointments = useMemo(() => {
    const search = keyword.trim().toLowerCase();

    return weekAppointments.filter(item => {
      const matchKeyword =
        !search ||
        String(item.maDatLich || '').toLowerCase().includes(search) ||
        String(item.tenBenhNhan || '').toLowerCase().includes(search) ||
        String(item.maBenhNhan || '').includes(search);

      const matchStatus =
        statusFilter === 'TatCa' ||
        item.trangThai === statusFilter;

      return matchKeyword && matchStatus;
    });
  }, [weekAppointments, keyword, statusFilter]);

  // Chia lịch hẹn theo từng thứ trong tuần
  const weekDays = useMemo(() => {
    const days = DAY_NAMES.map((name, i) => {
      const date = new Date(weekStart);
      date.setDate(weekStart.getDate() + i);

      return { name, date, items: [] };
    });

    filteredAppointments.forEach(item => {
      const d = parseDate(getAppointmentDate(item));
      if (!d) return;

      const index = Math.round((d - weekStart) / 86400000);

      if (index >= 0 && index <= 6) {
        days[index].items.push(item);
      }
    });

    // Sắp xếp theo giờ khám trong ngày
    days.forEach(day =>
      day.items.sort((a, b) =>
        String(a.gioBatDau || '').localeCompare(String(b.gioBatDau || ''))
      )
    );

    return days;
  }, [filteredAppointments, weekStart]);

  // Định dạng giờ khám
  const formatTime = (value) => {
    if (!value) return '--:--';

    return String(value).slice(0, 5);
  };

  // Hiển thị trạng thái
  const getStatus = (status) => {
    const statusMap = {
      ChoDuyet: {
        label: 'Chờ duyệt',
        className: 'bg-amber-100 text-amber-700'
      },
      DaDuyet: {
        label: 'Đã duyệt',
        className: 'bg-sky-100 text-sky-700'
      },
      HoanThanh: {
        label: 'Hoàn thành',
        className: 'bg-emerald-100 text-emerald-700'
      },
      DaHuy: {
        label: 'Đã hủy',
        className: 'bg-rose-100 text-rose-700'
      }
    };

    return statusMap[status] || {
      label: status || 'Không xác định',
      className: 'bg-slate-100 text-slate-600'
    };
  };

  // Duyệt lịch
  const handleApprove = async (id) => {
    const confirmed = window.confirm(
      'Bạn có chắc chắn muốn duyệt lịch hẹn này?'
    );

    if (!confirmed) return;

    try {
      await api.patch(`/lich-hen/${id}/duyet`);

      alert('Duyệt lịch hẹn thành công!');

      await fetchAppointments();
    } catch (err) {
      alert(
        err.response?.data?.message ||
        'Duyệt lịch hẹn thất bại!'
      );
    }
  };

  // Hủy lịch
  const handleCancel = async (id) => {
    const reason = window.prompt(
      'Nhập lý do hủy lịch hẹn:'
    );

    if (reason === null) return;

    try {
      await api.patch(
        `/lich-hen/${id}/huy`,
        null,
        {
          params: {
            lyDo: reason
          }
        }
      );

      alert('Hủy lịch hẹn thành công!');

      await fetchAppointments();
    } catch (err) {
      alert(
        err.response?.data?.message ||
        'Hủy lịch hẹn thất bại!'
      );
    }
  };

  const handleLogout = async () => {
    const confirmed = window.confirm(
      'Bạn có chắc chắn muốn đăng xuất?'
    );

    if (!confirmed) return;

    await logout();

    window.location.href = '/dang-nhap';
  };

  const StatCard = ({ title, value, color }) => (
    <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-5">
      <p className="text-sm text-slate-500">{title}</p>
      <p className={`text-3xl font-bold mt-2 ${color}`}>
        {value}
      </p>
    </div>
  );

  return (
    <div className="min-h-screen bg-slate-50">

   

      <main className="max-w-7xl mx-auto px-4 md:px-8 py-8">

        {/* Tiêu đề */}
        <div className="mb-6">
          <h2 className="text-2xl font-bold text-slate-800">
            Tổng quan lịch hẹn tuần này
          </h2>

          <p className="text-slate-500 mt-1">
            Theo dõi và xử lý lịch khám của bệnh nhân theo từng ngày trong tuần
          </p>
        </div>

        {/* Thống kê */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4 mb-8">

          <StatCard
            title="Tổng lịch hẹn"
            value={statistics.total}
            color="text-slate-800"
          />

          <StatCard
            title="Chờ duyệt"
            value={statistics.waiting}
            color="text-slate-800"
          />

          <StatCard
            title="Đã duyệt"
            value={statistics.approved}
            color="text-slate-800"
          />

          <StatCard
            title="Hoàn thành"
            value={statistics.completed}
            color="text-slate-800"
          />

          <StatCard
            title="Đã hủy"
            value={statistics.cancelled}
            color="text-slate-800"
          />

        </div>

        {/* Danh sách lịch hẹn */}
        <section className="bg-white rounded-xl shadow-sm border border-slate-200">

          <div className="p-5 border-b border-slate-200">
            <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4">

              <div>
                <h3 className="text-lg font-bold text-slate-800">
                  Lịch hẹn theo tuần
                </h3>

                <p className="text-sm text-slate-500 mt-1">
                  Quản lý và xác nhận lịch khám bệnh
                </p>
              </div>

              <button
                onClick={fetchAppointments}
                disabled={loading}
                className="bg-sky-500 hover:bg-sky-600 disabled:opacity-50 text-white px-4 py-2 rounded-lg text-sm font-medium"
              >
                Làm mới
              </button>

            </div>

            {/* Tìm kiếm và lọc */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3 mt-5">

              <input
                type="text"
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                placeholder="Tìm mã đặt lịch, tên hoặc mã bệnh nhân..."
                className="w-full border border-slate-300 rounded-lg px-4 py-2.5 outline-none focus:ring-2 focus:ring-sky-300 focus:border-sky-400"
              />

              <select
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                className="w-full border border-slate-300 rounded-lg px-4 py-2.5 outline-none focus:ring-2 focus:ring-sky-300 focus:border-sky-400"
              >
                <option value="TatCa">Tất cả trạng thái</option>
                <option value="ChoDuyet">Chờ duyệt</option>
                <option value="DaDuyet">Đã duyệt</option>
                <option value="HoanThanh">Hoàn thành</option>
                <option value="DaHuy">Đã hủy</option>
              </select>

            </div>
          </div>

          {/* Tiêu đề tuần */}
          <div className="px-5 py-4 border-b border-slate-200 bg-slate-50">
            <p className="font-semibold text-slate-800">
              Tuần {formatDate(weekStart)} - {formatDate(weekEnd)}
            </p>
            <p className="text-sm text-slate-500">
              {filteredAppointments.length} lịch hẹn trong tuần
            </p>
          </div>

          {/* Lịch theo từng ngày */}
          <div className="divide-y divide-slate-200">

            {loading ? (
              <p className="text-center py-10 text-slate-500">
                Đang tải danh sách lịch hẹn...
              </p>
            ) : error ? (
              <p className="text-center py-10 text-rose-600">
                {error}
              </p>
            ) : (
              weekDays.map((day) => (
                <div key={day.name}>

                  {/* Tiêu đề ngày */}
                  <div
                    className={`px-5 py-3 flex items-center justify-between ${
                      isToday(day.date) ? 'bg-sky-50' : 'bg-white'
                    }`}
                  >
                    <h4 className="font-bold text-slate-800">
                      {day.name}
                      <span className="ml-2 font-medium text-slate-500">
                        {formatDate(day.date)}
                      </span>

                      {isToday(day.date) && (
                        <span className="ml-2 text-xs bg-sky-500 text-white px-2 py-0.5 rounded-full">
                          Hôm nay
                        </span>
                      )}
                    </h4>

                    <span className="text-sm text-slate-500">
                      {day.items.length} lịch hẹn
                    </span>
                  </div>

                  {/* Danh sách lịch trong ngày */}
                  {day.items.length === 0 ? (
                    <p className="px-5 pb-4 text-sm text-slate-400">
                      Không có lịch hẹn
                    </p>
                  ) : (
                    <div className="overflow-x-auto">
                      <table className="w-full text-sm text-left">
                        <tbody className="divide-y divide-slate-100">

                          {day.items.map((item) => {
                            const status = getStatus(item.trangThai);

                            return (
                              <tr
                                key={item.maLichHen}
                                className="hover:bg-sky-50/50"
                              >
                                <td className="px-5 py-3 font-semibold text-slate-800 whitespace-nowrap w-36">
                                  {formatTime(item.gioBatDau)}
                                  {' - '}
                                  {formatTime(item.gioKetThuc)}
                                </td>

                                <td className="px-5 py-3 text-sky-700 font-medium whitespace-nowrap">
                                  {item.maDatLich}
                                </td>

                                <td className="px-5 py-3">
                                  {item.tenBenhNhan || 'Chưa có tên'}
                                  <span className="text-slate-400">
                                    {' '}(BN {item.maBenhNhan})
                                  </span>
                                </td>

                                <td className="px-5 py-3">
                                  <span className={`px-3 py-1 rounded-full text-xs font-semibold ${status.className}`}>
                                    {status.label}
                                  </span>
                                </td>

                                <td className="px-5 py-3">
                                  <div className="flex items-center justify-end gap-2">

                                    {item.trangThai === 'ChoDuyet' && (
                                      <>
                                        <button
                                          onClick={() => handleApprove(item.maLichHen)}
                                          className="bg-emerald-500 hover:bg-emerald-600 text-white px-3 py-2 rounded-lg text-xs font-medium whitespace-nowrap"
                                        >
                                          Duyệt
                                        </button>

                                        <button
                                          onClick={() => handleCancel(item.maLichHen)}
                                          className="bg-rose-500 hover:bg-rose-600 text-white px-3 py-2 rounded-lg text-xs font-medium whitespace-nowrap"
                                        >
                                          Hủy
                                        </button>
                                      </>
                                    )}

                                    {item.trangThai === 'DaDuyet' && (
                                      <button
                                        onClick={() => handleCancel(item.maLichHen)}
                                        className="bg-rose-500 hover:bg-rose-600 text-white px-3 py-2 rounded-lg text-xs font-medium whitespace-nowrap"
                                      >
                                        Hủy lịch
                                      </button>
                                    )}

                                    {item.trangThai === 'HoanThanh' && (
                                      <span className="text-emerald-600 font-medium">
                                        Đã khám
                                      </span>
                                    )}

                                    {item.trangThai === 'DaHuy' && (
                                      <span className="text-slate-400">
                                        Đã hủy
                                      </span>
                                    )}

                                  </div>
                                </td>
                              </tr>
                            );
                          })}

                        </tbody>
                      </table>
                    </div>
                  )}
                </div>
              ))
            )}

          </div>

          <div className="px-5 py-4 border-t border-slate-200 text-sm text-slate-500">
            Hiển thị {filteredAppointments.length} lịch hẹn của tuần này
          </div>

        </section>

      </main>
    </div>
  );
}