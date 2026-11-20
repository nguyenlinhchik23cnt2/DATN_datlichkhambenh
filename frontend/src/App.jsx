import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import ChatWidget from './components/ChatWidget';
import ProtectedRoute from './components/ProtectedRoute';

import HomePage from './pages/HomePage';
import DatLichPage from './pages/DatLichPage';
import TraCuuPage from './pages/TraCuuPage';
import DangNhapPage from './pages/DangNhapPage';
import DoctorDashboard from './pages/doctor/DoctorDashboard';
import AdminDashboard from './pages/admin/AdminDashboard';
import ReceptionistDashboard from './pages/receptionist/ReceptionistDashboard';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <div className="min-h-screen flex flex-col bg-slate-50 text-slate-800">
          <Navbar />
          <main className="flex-1">
            <Routes>
              {/* Public Routes */}
              <Route path="/" element={<HomePage />} />
              <Route path="/dat-lich" element={<DatLichPage />} />
              <Route path="/tra-cuu" element={<TraCuuPage />} />
              <Route path="/dang-nhap" element={<DangNhapPage />} />

              {/* Doctor Protected Route */}
              <Route
                path="/doctor"
                element={
                  <ProtectedRoute allowedRoles={['BacSi']}>
                    <DoctorDashboard />
                  </ProtectedRoute>
                }
              />

              {/* Admin Protected Route */}
              <Route
                path="/admin"
                element={
                  <ProtectedRoute allowedRoles={['Admin']}>
                    <AdminDashboard />
                  </ProtectedRoute>
                }
              />

              {/* Receptionist Protected Route */}
              <Route
                path="/tiep-tan"
                element={
                  <ProtectedRoute allowedRoles={['TiepTan']}>
                    <ReceptionistDashboard />
                  </ProtectedRoute>
                }
              />

              {/* Fallback */}
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </main>
          <Footer />

          {/* Nút chat AI nổi, hiện ở mọi trang */}
          <ChatWidget />
        </div>
      </BrowserRouter>
    </AuthProvider>
  );
}