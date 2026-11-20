
import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function ProtectedRoute({ children, allowedRoles }) {
  const { user, isAuthenticated } = useAuth();

  // Chưa đăng nhập
  if (!isAuthenticated || !user) {
    return <Navigate to="/dang-nhap" replace />;
  }

  // Không đúng vai trò
  if (
    allowedRoles &&
    !allowedRoles.includes(user.vaiTro)
  ) {
    // Chuyển người dùng về trang tương ứng với vai trò
    switch (user.vaiTro) {
      case 'Admin':
        return <Navigate to="/admin" replace />;

      case 'BacSi':
        return <Navigate to="/doctor" replace />;

      case 'TiepTan':
        return <Navigate to="/tiep-tan" replace />;

      default:
        return <Navigate to="/" replace />;
    }
  }

  return children;
}
