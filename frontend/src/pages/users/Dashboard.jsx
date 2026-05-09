import React, {useEffect, useState} from 'react';
import '../../styles/Dashboard.css';

function Dashboard({onNavigate}) {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    async function load() {
      try {
        const token = localStorage.getItem('token');
        const res = await fetch('/api/dashboard', {
          headers: {
            'Content-Type': 'application/json',
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
          }
        });

        // If unauthorized, send user back to login
        if (res.status === 401) {
          window.location.href = '/login';
          return;
        }

        const data = await res.json();
        if (!res.ok) throw new Error(data?.message || 'Failed to load assets');
        setStats(data || null);
      } catch (e) {
        setError(e.message || 'Error');
      } finally {
        setLoading(false);
      }
    }
    load();
  }, []);

  const total = stats?.totalAssets ?? 0;
  const availableAssets = stats?.availableAssets ?? 0;
  const assignedAssets = stats?.assignedAssets ?? 0;
  const byType = stats?.typeCounts || {};
  const byStatus = stats?.statusCounts || {};
  const byAssignedUser = stats?.assignedUserCounts || {};

  const statusKeys = Object.keys(byStatus);
  const maxStatusCount = Math.max(1, ...Object.values(byStatus));

  if (loading) return <div>Loading dashboard...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <div className="dashboard-page">
      <div className="dashboard-header">
        <div className="dashboard-header-left">
          <h2>Inventory Dashboard</h2>
          <p>Current assets by type, status, and assigned user.</p>
        </div>

        <div className="dashboard-header-actions">
          <button
            type="button"
            className="notif-bell"
            aria-label="Open reports"
            onClick={() => onNavigate && onNavigate('reports')}
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M3 12c0-3.314 2.686-6 6-6h2V4h2v2h2c3.314 0 6 2.686 6 6v8c0 3.314-2.686 6-6 6H9c-3.314 0-6-2.686-6-6v-8z" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
              <path d="M7 8h10M7 12h10M7 16h5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </button>
          <button
            type="button"
            className="notif-bell"
            aria-label="Open notifications"
            onClick={() => onNavigate && onNavigate('notifications')}
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M15 17H9" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
              <path d="M12 22c1.1046 0 2-.8954 2-2h-4c0 1.1046.8954 2 2 2z" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
              <path d="M18 8c0-3.3137-2.6863-6-6-6S6 4.6863 6 8v3.586c0 .797-.316 1.564-.879 2.121L4 16h16l-1.121-2.293C18.316 13.15 18 12.382 18 11.586V8z" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </button>
        </div>
      </div>

      <div className="dashboard-summary-grid">
        <div className="dashboard-card dashboard-stat-card">
          <div className="dashboard-label">Total assets</div>
          <div className="dashboard-value">{total}</div>
        </div>
        <div className="dashboard-card dashboard-stat-card">
          <div className="dashboard-label">Available</div>
          <div className="dashboard-value">{availableAssets}</div>
        </div>
        <div className="dashboard-card dashboard-stat-card">
          <div className="dashboard-label">Assigned</div>
          <div className="dashboard-value">{assignedAssets}</div>
        </div>
        <div className="dashboard-card dashboard-stat-card">
          <div className="dashboard-label">Types</div>
          <div className="dashboard-value dashboard-value-small">{Object.keys(byType).length}</div>
        </div>
        <div className="dashboard-card dashboard-stat-card">
          <div className="dashboard-label">Assigned users</div>
          <div className="dashboard-value dashboard-value-small">{Object.keys(byAssignedUser).length}</div>
        </div>
      </div>

      <div className="dashboard-content-grid">
        <section className="dashboard-panel dashboard-chart-panel">
          <h3>Status distribution</h3>
          <div className="dashboard-chart-wrap">
            <svg width="100%" height="100%" viewBox={`0 0 100 ${statusKeys.length * 20}`}>
              {statusKeys.map((k, i) => {
                const value = Number(byStatus[k] || 0);
                const barWidth = (value / maxStatusCount) * 80;
                return (
                  <g key={k} transform={`translate(0, ${i * 20})`}>
                    <text x={0} y={12} fontSize={6} fill="#344054">{k} ({value})</text>
                    <rect x={30} y={4} width={barWidth} height={10} fill="#3b82f6" rx={2} />
                  </g>
                );
              })}
            </svg>
          </div>
        </section>

        <section className="dashboard-panel dashboard-side-panel">
          <h3>Breakdown by type</h3>
          <div className="dashboard-list-card">
            {Object.entries(byType).map(([k,v]) => (
              <div key={k} className="dashboard-list-row">
                <div className="dashboard-list-key">{k}</div>
                <div className="dashboard-list-value">{v}</div>
              </div>
            ))}
          </div>

          <h3 className="dashboard-section-title">Top assigned users</h3>
          <div className="dashboard-list-card">
            {Object.entries(byAssignedUser)
              .sort((a,b)=>b[1]-a[1])
              .slice(0,6)
              .map(([k,v])=> (
                <div key={k} className="dashboard-list-row">
                  <div className="dashboard-list-key">{k}</div>
                  <div className="dashboard-list-value">{v}</div>
                </div>
              ))}
          </div>
        </section>
      </div>
    </div>
  );
}

export default Dashboard;
