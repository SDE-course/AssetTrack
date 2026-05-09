
import './services/App.css';
import React, {useState} from 'react';
import Dashboard from './pages/users/Dashboard';
import Notifications from './pages/users/Notifications';
import Reports from './pages/users/Reports';

import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/auth/Login';
import Register from './pages/auth/Register';


function App() {
  const [page, setPage] = useState('dashboard');
  const navigate = (p) => setPage(p);

  return (
    <>
      {page === 'dashboard' && <Dashboard onNavigate={navigate} />}
      {page === 'notifications' && <Notifications onNavigate={navigate} />}
      {page === 'reports' && <Reports onNavigate={navigate} />}
    </>
  );
}

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