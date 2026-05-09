import React, { useEffect, useState } from 'react';
import '../../styles/Report.css';

function Reports({ onNavigate }) {
	const [stats, setStats] = useState(null);
	const [loading, setLoading] = useState(true);
	const [error, setError] = useState(null);

	useEffect(() => {
		async function loadReports() {
			try {
				const res = await fetch('/api/reports/usage-statistics');
				if (!res.ok) throw new Error('Failed to load reports');
				const data = await res.json();
				setStats(data);
			} catch (e) {
				setError(e.message || 'Error loading reports');
			} finally {
				setLoading(false);
			}
		}

		loadReports();
	}, []);

	if (loading) return <div className="reports-page">Loading reports...</div>;
	if (error) return <div className="reports-page">Error: {error}</div>;
	if (!stats) return <div className="reports-page">No data available</div>;

	const allocationKeys = Object.keys(stats.allocationsByType || {});
	const maxAllocations = Math.max(1, ...Object.values(stats.allocationsByType || {}));

	return (
		<div className="reports-page">
			<div className="reports-container">
				<header className="reports-header">
					<div>
						<p className="reports-kicker">Analytics</p>
						<h2>Usage Reports</h2>
						<p className="reports-subtitle">Asset allocation history, usage statistics, and condition tracking.</p>
					</div>
					<button
						type="button"
						className="reports-back-button"
						onClick={() => onNavigate && onNavigate('dashboard')}
					>
						← Back to dashboard
					</button>
				</header>

				{/* Summary Stats */}
				<div className="reports-summary-grid">
					<div className="reports-card reports-stat-card">
						<div className="reports-label">Total assets</div>
						<div className="reports-value">{stats.totalAssets}</div>
					</div>
					<div className="reports-card reports-stat-card">
						<div className="reports-label">Total allocations</div>
						<div className="reports-value">{stats.totalAllocations}</div>
					</div>
					<div className="reports-card reports-stat-card">
						<div className="reports-label">Active allocations</div>
						<div className="reports-value">{stats.activeAllocations}</div>
					</div>
					<div className="reports-card reports-stat-card">
						<div className="reports-label">Avg. duration (days)</div>
						<div className="reports-value">{Math.round(stats.averageAllocationDurationDays)}</div>
					</div>
				</div>

				{/* Main content grid */}
				<div className="reports-content-grid">
					{/* Allocations by Type Chart */}
					<section className="reports-panel">
						<h3>Allocations by asset type</h3>
						<div className="reports-chart-wrap">
							<svg width="100%" height="100%" viewBox={`0 0 100 ${allocationKeys.length * 20}`}>
								{allocationKeys.map((k, i) => {
									const value = stats.allocationsByType[k] || 0;
									const barWidth = (value / maxAllocations) * 80;
									return (
										<g key={k} transform={`translate(0, ${i * 20})`}>
											<text x={0} y={12} fontSize={6} fill="#cbd5e1">
												{k} ({value})
											</text>
											<rect x={30} y={4} width={barWidth} height={10} fill="#8b5cf6" rx={2} />
										</g>
									);
								})}
							</svg>
						</div>
					</section>

					{/* Top Used Assets */}
					<section className="reports-panel">
						<h3>Most used assets</h3>
						<div className="reports-list-card">
							{stats.topUsedAssets && stats.topUsedAssets.length > 0 ? (
								stats.topUsedAssets.map((asset, idx) => (
									<div key={idx} className="reports-list-row">
										<div className="reports-list-item">
											<div className="reports-list-key">{asset.assetTag}</div>
											<div className="reports-list-subtext">{asset.assetName}</div>
										</div>
										<div className="reports-list-value">{asset.allocationCount} uses</div>
									</div>
								))
							) : (
								<p>No asset usage data</p>
							)}
						</div>
					</section>
				</div>

				{/* User Allocation Stats */}
				<section className="reports-panel reports-wide">
					<h3>User allocation statistics</h3>
					<div className="reports-list-card">
						{stats.userAllocationStats && stats.userAllocationStats.length > 0 ? (
							<div className="reports-table">
								<div className="reports-table-header">
									<div className="reports-table-cell">User</div>
									<div className="reports-table-cell">Total allocations</div>
									<div className="reports-table-cell">Active</div>
								</div>
								{stats.userAllocationStats.map((user, idx) => (
									<div key={idx} className="reports-table-row">
										<div className="reports-table-cell">{user.userName}</div>
										<div className="reports-table-cell">{user.allocationCount}</div>
										<div className="reports-table-cell">{user.activeAllocations}</div>
									</div>
								))}
							</div>
						) : (
							<p>No user allocation data</p>
						)}
					</div>
				</section>

				{/* Recent Allocations / Allocation History */}
				<section className="reports-panel reports-wide">
					<h3>Recent allocation history</h3>
					<div className="reports-list-card">
						{stats.recentAllocations && stats.recentAllocations.length > 0 ? (
							<div className="reports-table">
								<div className="reports-table-header">
									<div className="reports-table-cell">Asset</div>
									<div className="reports-table-cell">User</div>
									<div className="reports-table-cell">Assigned</div>
									<div className="reports-table-cell">Returned</div>
									<div className="reports-table-cell">Status</div>
								</div>
								{stats.recentAllocations.map((allocation, idx) => (
									<div key={idx} className="reports-table-row">
										<div className="reports-table-cell">
											<div className="reports-table-key">{allocation.assetTag}</div>
											<div className="reports-table-subtext">{allocation.assetName}</div>
										</div>
										<div className="reports-table-cell">{allocation.assignedToUser}</div>
										<div className="reports-table-cell">
											{new Date(allocation.assignedDate).toLocaleDateString()}
										</div>
										<div className="reports-table-cell">
											{allocation.returnedDate ? new Date(allocation.returnedDate).toLocaleDateString() : '—'}
										</div>
										<div className="reports-table-cell">
											<span className={`reports-status-badge ${allocation.active ? 'active' : 'returned'}`}>
												{allocation.active ? 'Active' : 'Returned'}
											</span>
										</div>
									</div>
								))}
							</div>
						) : (
							<p>No allocation history</p>
						)}
					</div>
				</section>
			</div>
		</div>
	);
}

export default Reports;
