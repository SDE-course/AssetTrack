import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../../styles/Dashboard.css';

function getUser() {
  try { return JSON.parse(localStorage.getItem('user')) || {}; } catch { return {}; }
}

const API_BASE = process.env.REACT_APP_API_URL || 'http://localhost:8080';

export default function Dashboard() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  const user = getUser();

  useEffect(() => {
    async function load() {
      try {
        const token = localStorage.getItem('token');
        const res = await fetch(`${API_BASE}/api/dashboard`, {
          headers: {
            'Content-Type': 'application/json',
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
          }
        });
        if (res.status === 401) { window.location.href = '/login'; return; }
        const data = await res.json();
        if (!res.ok) throw new Error(data?.message || 'Failed to load dashboard');
        setStats(data || null);
      } catch (e) {
        setError(e.message || 'Error');
      } finally {
        setLoading(false);
      }
    }
    load();
  }, []);

  const total           = stats?.totalAssets ?? 0;
  const availableAssets = stats?.availableAssets ?? 0;
  const assignedAssets  = stats?.assignedAssets ?? 0;
  const byType          = stats?.typeCounts || {};
  const byStatus        = stats?.statusCounts || {};
  const byAssignedUser  = stats?.assignedUserCounts || {};

  const statusKeys = Object.keys(byStatus);
  const maxStatusCount = Math.max(1, ...Object.values(byStatus));

  const statusColors = {
    AVAILABLE: '#34d399',
    ASSIGNED: '#60a5fa',
    UNDER_MAINTENANCE: '#fbbf24',
    DECOMMISSIONED: '#f87171',
  };

  if (loading) return <div className="dashboard-loading">Loading dashboard…</div>;
  if (error)   return <div className="dashboard-error">Error: {error}</div>;

  const utilizationPct = total > 0 ? Math.round((assignedAssets / total) * 100) : 0;

  return (
    <div className="dashboard-page">
      {/* Header */}
      <div className="dashboard-header">
        <div className="dashboard-header-left">
          <p className="dashboard-eyebrow">Welcome back, {user.name?.split(' ')[0] || 'there'}</p>
          <h2>Inventory Dashboard</h2>
          <p className="dashboard-sub">Real-time overview of your asset fleet</p>
        </div>
        <div className="dashboard-header-actions">
          <button className="dash-action-btn" onClick={() => navigate('/assets')}>
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <rect x="2" y="7" width="20" height="14" rx="2"/>
              <path d="M16 7V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v2"/>
            </svg>
            Assets
          </button>
          <button className="dash-action-btn" onClick={() => navigate('/notifications')}>
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
              <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
            </svg>
            Alerts
          </button>
        </div>
      </div>

      {/* Stat cards */}
      <div className="dashboard-summary-grid">
        <div className="dashboard-card dashboard-stat-card stat-total">
          <div className="stat-icon stat-icon-blue">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
            </svg>
          </div>
          <div>
            <div className="dashboard-label">Total Assets</div>
            <div className="dashboard-value">{total}</div>
          </div>
        </div>

        <div className="dashboard-card dashboard-stat-card stat-available">
          <div className="stat-icon stat-icon-green">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
          </div>
          <div>
            <div className="dashboard-label">Available</div>
            <div className="dashboard-value">{availableAssets}</div>
          </div>
        </div>

        <div className="dashboard-card dashboard-stat-card stat-assigned">
          <div className="stat-icon stat-icon-purple">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
            </svg>
          </div>
          <div>
            <div className="dashboard-label">Assigned</div>
            <div className="dashboard-value">{assignedAssets}</div>
          </div>
        </div>

        <div className="dashboard-card dashboard-stat-card stat-utilization">
          <div className="stat-icon stat-icon-amber">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <line x1="18" y1="20" x2="18" y2="10"/>
              <line x1="12" y1="20" x2="12" y2="4"/>
              <line x1="6" y1="20" x2="6" y2="14"/>
            </svg>
          </div>
          <div>
            <div className="dashboard-label">Utilization</div>
            <div className="dashboard-value">{utilizationPct}<span className="unit">%</span></div>
          </div>
        </div>

        <div className="dashboard-card dashboard-stat-card">
          <div className="stat-icon stat-icon-cyan">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/>
              <rect x="14" y="14" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/>
            </svg>
          </div>
          <div>
            <div className="dashboard-label">Asset Types</div>
            <div className="dashboard-value dashboard-value-small">{Object.keys(byType).length}</div>
          </div>
        </div>
      </div>

      {/* Charts + lists */}
      <div className="dashboard-content-grid">
        {/* Bar chart — status distribution */}
        <section className="dashboard-panel dashboard-chart-panel">
          <div className="panel-hd">
            <h3>Status Distribution</h3>
            <span className="panel-count">{total} total</span>
          </div>
          <div className="dashboard-chart-wrap">
            {statusKeys.map((k) => {
              const value = Number(byStatus[k] || 0);
              const pct = Math.round((value / maxStatusCount) * 100);
              const color = statusColors[k] || '#6366f1';
              return (
                <div key={k} className="chart-bar-row">
                  <span className="chart-bar-label">{k.replace(/_/g, ' ')}</span>
                  <div className="chart-bar-track">
                    <div
                      className="chart-bar-fill"
                      style={{ width: `${pct}%`, background: color }}
                    />
                  </div>
                  <span className="chart-bar-value">{value}</span>
                </div>
              );
            })}
          </div>

          {/* Utilization ring */}
          <div className="utilization-ring-row">
            <svg width="72" height="72" viewBox="0 0 72 72">
              <circle cx="36" cy="36" r="28" fill="none" stroke="rgba(255,255,255,0.06)" strokeWidth="8"/>
              <circle
                cx="36" cy="36" r="28"
                fill="none"
                stroke="#6366f1"
                strokeWidth="8"
                strokeLinecap="round"
                strokeDasharray={`${(utilizationPct / 100) * 175.9} 175.9`}
                strokeDashoffset="43.98"
                transform="rotate(-90 36 36)"
              />
            </svg>
            <div className="utilization-ring-text">
              <span className="ring-pct">{utilizationPct}%</span>
              <span className="ring-label">in use</span>
            </div>
          </div>
        </section>

        {/* Type breakdown + top users */}
        <section className="dashboard-panel dashboard-side-panel">
          <div className="panel-hd">
            <h3>By Type</h3>
          </div>
          <div className="dashboard-list-card">
            {Object.entries(byType).map(([k, v]) => (
              <div key={k} className="dashboard-list-row">
                <div className="dashboard-list-key">
                  <span className="type-dot" style={{background: k === 'LAPTOP' ? '#6366f1' : k === 'MONITOR' ? '#10b981' : '#f59e0b'}} />
                  {k}
                </div>
                <div className="dashboard-list-value">{v}</div>
              </div>
            ))}
            {Object.keys(byType).length === 0 && <p className="list-empty">No data</p>}
          </div>

          <div className="panel-hd" style={{marginTop: 20}}>
            <h3>Top Holders</h3>
          </div>
          <div className="dashboard-list-card">
            {Object.entries(byAssignedUser)
              .sort((a, b) => b[1] - a[1])
              .slice(0, 5)
              .map(([k, v], i) => (
                <div key={k} className="dashboard-list-row">
                  <div className="dashboard-list-key">
                    <span className="rank-badge">{i + 1}</span>
                    {k}
                  </div>
                  <div className="dashboard-list-value">{v}</div>
                </div>
              ))}
            {Object.keys(byAssignedUser).length === 0 && <p className="list-empty">No assignments yet</p>}
          </div>
        </section>
      </div>

      {/* Quick actions */}
      <section className="dashboard-quick-actions">
        <h3 className="quick-title">Quick Actions</h3>
        <div className="quick-grid">
          <button className="quick-card" onClick={() => navigate('/assets')}>
            <span className="quick-icon">📦</span>
            <span className="quick-label">View All Assets</span>
          </button>
          <button className="quick-card" onClick={() => navigate('/allocation')}>
            <span className="quick-icon">🔄</span>
            <span className="quick-label">Allocation Console</span>
          </button>
          <button className="quick-card" onClick={() => navigate('/notifications')}>
            <span className="quick-icon">🔔</span>
            <span className="quick-label">Notifications</span>
          </button>
          <button className="quick-card" onClick={() => navigate('/reports')}>
            <span className="quick-icon">📊</span>
            <span className="quick-label">Reports</span>
          </button>
        </div>
      </section>
    </div>
  );
}