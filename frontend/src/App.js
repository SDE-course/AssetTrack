import './App.css';
import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import AppLayout from './components/layout/AppLayout';
import Dashboard from './pages/users/Dashboard';
import Notifications from './pages/users/Notifications';
import Reports from './pages/users/Reports';
import Login from './pages/auth/Login';
import Register from './pages/auth/Register';
import AssetAllocationConsole from './components/allocation/AssetAllocationConsole';
import AssetsPage from './pages/users/AssetsPage';
import UsersPage from './pages/users/UsersPage';

function RequireAuth({ children }) {
  const token = localStorage.getItem('token');
  if (!token) return <Navigate to="/login" replace />;
  return <AppLayout>{children}</AppLayout>;
}

function App() {
  return (
    <Routes>
      <Route path="/login"    element={<Login />} />
      <Route path="/register" element={<Register />} />

      <Route path="/dashboard"    element={<RequireAuth><Dashboard /></RequireAuth>} />
      <Route path="/assets"       element={<RequireAuth><AssetsPage /></RequireAuth>} />
      <Route path="/allocation"   element={<RequireAuth><AssetAllocationConsole /></RequireAuth>} />
      <Route path="/users"        element={<RequireAuth><UsersPage /></RequireAuth>} />
      <Route path="/reports"      element={<RequireAuth><Reports /></RequireAuth>} />
      <Route path="/notifications" element={<RequireAuth><Notifications /></RequireAuth>} />

      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}

export default App;