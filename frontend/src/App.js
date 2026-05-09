import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/auth/Login';
import Register from './pages/auth/Register';

function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>


// import AssetAllocationConsole from './components/allocation/AssetAllocationConsole';
// import './App.css';
// import UsersPage from './pages/users/UsersPage';

// function App() {
//   return (
//     <>
//       <UsersPage />
//       <AssetAllocationConsole />
//     </>
//   );
// }

export default App;