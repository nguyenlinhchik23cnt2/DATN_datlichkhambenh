
import React, {
  createContext,
  useContext,
  useState
} from 'react';

import api from '../api/client';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {

  // Lấy thông tin tài khoản đã lưu
  const [user, setUser] = useState(() => {
    try {
      const saved = localStorage.getItem('user');
      return saved ? JSON.parse(saved) : null;
    } catch {
      localStorage.removeItem('user');
      return null;
    }
  });

  // Lưu thông tin đăng nhập
  const saveUser = (userData) => {
    setUser(userData);
    localStorage.setItem('user', JSON.stringify(userData));
  };

  // Đăng nhập
  const login = async (tenDangNhap, matKhau) => {
    try {
      const res = await api.post('/tai-khoan/dang-nhap', {
        tenDangNhap,
        matKhau
      });

      const userData = res.data;

      if (!userData || !userData.vaiTro) {
        return {
          success: false,
          message: 'Thông tin tài khoản trả về không hợp lệ!'
        };
      }

      saveUser(userData);

      return {
        success: true,
        user: userData
      };

    } catch (err) {
      const message =
        err.response?.data?.message ||
        'Tên đăng nhập hoặc mật khẩu không chính xác!';

      return {
        success: false,
        message
      };
    }
  };

  // Đăng ký
  const register = async (dangKyData) => {
    try {
      const res = await api.post(
        '/tai-khoan/dang-ky',
        dangKyData
      );

      const userData = res.data;

      if (!userData || !userData.vaiTro) {
        return {
          success: false,
          message: 'Thông tin đăng ký trả về không hợp lệ!'
        };
      }

      saveUser(userData);

      return {
        success: true,
        user: userData
      };

    } catch (err) {
      const message =
        err.response?.data?.message ||
        'Đăng ký không thành công!';

      return {
        success: false,
        message
      };
    }
  };

  // Đăng xuất
  const logout = async () => {
    try {
      await api.post('/tai-khoan/dang-xuat');
    } catch (err) {
      console.error('Lỗi đăng xuất Backend:', err);
    } finally {
      setUser(null);
      localStorage.removeItem('user');
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        login,
        register,
        logout,
        isAuthenticated: !!user
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

// Hook sử dụng AuthContext
export const useAuth = () => {
  return useContext(AuthContext);
};
