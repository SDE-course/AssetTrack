import './services/App.css';
import React, {useState} from 'react';
import Dashboard from './pages/users/Dashboard';
import Notifications from './pages/users/Notifications';
import Reports from './pages/users/Reports';

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

export default App;
