import React, { useEffect, useState } from 'react';
import '../../styles/Report.css';

function getUser() {
	try { return JSON.parse(localStorage.getItem('user')) || {}; } catch { return {}; }
}

function Reports() {
	const user = getUser();
	const role = user.role || '';
	const canViewConditionReports = role === 'ADMIN' || role === 'MANAGER';

	const [stats, setStats] = useState(null);
	const [loading, setLoading] = useState(true);
	const [error, setError] = useState(null);

	// Condition reports state (admin/manager only)
	const [conditionReports, setConditionReports] = useState([]);
	const [conditionLoading, setConditionLoading] = useState(false);
	const [conditionFilter, setConditionFilter] = useState('');
	const [conditionPage, setConditionPage] = useState(0);
	const [conditionTotalPages, setConditionTotalPages] = useState(1);
	const [updatingId, setUpdatingId] = useState(null);

	// ── Load usage statistics ──────────────────────────────────────────────
	useEffect(() => {
		async function loadReports() {
			try {
				const token = localStorage.getItem('token');
				const res = await fetch('/api/reports/usage-statistics', {
					headers: {
						Authorization: token ? `Bearer ${token}` : undefined,
					},
				});
				if (!res.ok) throw new Error(`Failed to load reports (${res.status})`);
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

	// ── Load condition reports (admin/manager only) ────────────────────────
	useEffect(() => {
		if (!canViewConditionReports) return;

		async function loadConditionReports() {
			setConditionLoading(true);
			try {
				const token = localStorage.getItem('token');
				const statusParam = conditionFilter ? `&status=${conditionFilter}` : '';
				const res = await fetch(
					`/api/condition-reports?page=${conditionPage}&size=15${statusParam}`,
					{ headers: { Authorization: token ? `Bearer ${token}` : undefined } }
				);
				if (!res.ok) throw new Error('Failed to load condition reports');
				const data = await res.json();
				const rows = Array.isArray(data?.content) ? data.content : Array.isArray(data) ? data : [];
				setConditionReports(rows);
				setConditionTotalPages(Math.max(1, Number(data?.totalPages ?? 1)));
			} catch (e) {
				console.error(e);
			} finally {
				setConditionLoading(false);
			}
		}
		loadConditionReports();
	}, [canViewConditionReports, conditionFilter, conditionPage]);

	async function updateConditionReport(id, status, adminNotes) {
		setUpdatingId(id);
		try {
			const token = localStorage.getItem('token');
			const res = await fetch(`/api/condition-reports/${id}`, {
				method: 'PUT',
				headers: {
					'Content-Type': 'application/json',
					Authorization: token ? `Bearer ${token}` : undefined,
				},
				body: JSON.stringify({ status, adminNotes }),
			});
			if (!res.ok) throw new Error('Update failed');
			const updated = await res.json();
			setConditionReports((prev) =>
				prev.map((r) => (r.id === id ? updated : r))
			);
		} catch (e) {
			alert(e.message || 'Failed to update report');
		} finally {
			setUpdatingId(null);
		}
	}

	// ── Render ─────────────────────────────────────────────────────────────

	if (loading) return <div className="reports-page"><div className="reports-loading">Loading reports…</div></div>;
	if (error) return <div className="reports-page"><div className="reports-error">Error: {error}</div></div>;
	if (!stats) return <div className="reports-page"><div className="reports-loading">No data available</div></div>;

	const allocationKeys = Object.keys(stats.allocationsByType || {});
	const maxAllocations = Math.max(1, ...Object.values(stats.allocationsByType || {}));

	function statusBadgeClass(status) {
		switch (status) {
			case 'OPEN': return 'condition-badge condition-badge--open';
			case 'IN_PROGRESS': return 'condition-badge condition-badge--progress';
			case 'RESOLVED': return 'condition-badge condition-badge--resolved';
			default: return 'condition-badge';
		}
	}

	return (
		<div className="reports-page">
			<div className="reports-container">
				<header className="reports-header">
					<div>
						<p className="reports-kicker">Analytics</p>
						<h2>Usage Reports</h2>
						<p className="reports-subtitle">Asset allocation history, usage statistics, and condition tracking.</p>
					</div>
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

				{/* Recent Allocations */}
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

				{/* ── Asset Condition Reports (Admin / Manager only) ──────────────── */}
				{canViewConditionReports && (
					<section className="reports-panel reports-wide">
						<div className="reports-condition-header">
							<h3>Asset condition reports</h3>
							<div className="reports-condition-filters">
								{['', 'OPEN', 'IN_PROGRESS', 'RESOLVED'].map((s) => (
									<button
										key={s}
										type="button"
										className={`reports-condition-filter-btn ${conditionFilter === s ? 'active' : ''}`}
										onClick={() => { setConditionFilter(s); setConditionPage(0); }}
									>
										{s || 'All'}
									</button>
								))}
							</div>
						</div>

						{conditionLoading ? (
							<div className="reports-loading">Loading condition reports…</div>
						) : conditionReports.length === 0 ? (
							<div className="reports-empty">No condition reports found.</div>
						) : (
							<div className="reports-table reports-condition-table">
								<div className="reports-table-header reports-condition-row">
									<div className="reports-table-cell">Asset</div>
									<div className="reports-table-cell">Reported by</div>
									<div className="reports-table-cell">Issue</div>
									<div className="reports-table-cell">Description</div>
									<div className="reports-table-cell">Status</div>
									<div className="reports-table-cell">Reported</div>
									<div className="reports-table-cell">Actions</div>
								</div>
								{conditionReports.map((report) => (
									<ConditionReportRow
										key={report.id}
										report={report}
										isUpdating={updatingId === report.id}
										onUpdate={updateConditionReport}
										statusBadgeClass={statusBadgeClass}
									/>
								))}
							</div>
						)}

						{/* Pagination */}
						{conditionTotalPages > 1 && (
							<div className="reports-pagination">
								<span>Page {conditionPage + 1} of {conditionTotalPages}</span>
								<button
									type="button"
									className="reports-back-button"
									disabled={conditionPage === 0}
									onClick={() => setConditionPage((p) => Math.max(0, p - 1))}
								>
									Prev
								</button>
								<button
									type="button"
									className="reports-back-button"
									disabled={conditionPage >= conditionTotalPages - 1}
									onClick={() => setConditionPage((p) => Math.min(conditionTotalPages - 1, p + 1))}
								>
									Next
								</button>
							</div>
						)}
					</section>
				)}
			</div>
		</div>
	);
}

// ── Inline editable row for condition reports ──────────────────────────────
function ConditionReportRow({ report, isUpdating, onUpdate, statusBadgeClass }) {
	const [editStatus, setEditStatus] = useState(report.status);
	const [editNotes, setEditNotes] = useState(report.adminNotes || '');
	const [editing, setEditing] = useState(false);

	function handleSave() {
		onUpdate(report.id, editStatus, editNotes);
		setEditing(false);
	}

	return (
		<div className="reports-table-row reports-condition-row">
			<div className="reports-table-cell">
				<div className="reports-table-key">{report.assetSerial}</div>
				<div className="reports-table-subtext">{report.assetName}</div>
			</div>
			<div className="reports-table-cell">{report.reportedByName}</div>
			<div className="reports-table-cell">{report.issueType}</div>
			<div className="reports-table-cell reports-description-cell">{report.description}</div>
			<div className="reports-table-cell">
				{editing ? (
					<select
						className="reports-condition-select"
						value={editStatus}
						onChange={(e) => setEditStatus(e.target.value)}
					>
						<option value="OPEN">Open</option>
						<option value="IN_PROGRESS">In Progress</option>
						<option value="RESOLVED">Resolved</option>
					</select>
				) : (
					<span className={statusBadgeClass(report.status)}>
						{report.status.replace('_', ' ')}
					</span>
				)}
			</div>
			<div className="reports-table-cell">
				{report.reportedAt ? new Date(report.reportedAt).toLocaleDateString() : '—'}
			</div>
			<div className="reports-table-cell">
				{editing ? (
					<div className="reports-condition-actions">
						<input
							className="reports-condition-notes-input"
							placeholder="Admin notes…"
							value={editNotes}
							onChange={(e) => setEditNotes(e.target.value)}
						/>
						<button
							type="button"
							className="reports-condition-save-btn"
							onClick={handleSave}
							disabled={isUpdating}
						>
							{isUpdating ? '…' : 'Save'}
						</button>
						<button
							type="button"
							className="reports-condition-cancel-btn"
							onClick={() => { setEditing(false); setEditStatus(report.status); setEditNotes(report.adminNotes || ''); }}
						>
							Cancel
						</button>
					</div>
				) : (
					<button
						type="button"
						className="reports-condition-edit-btn"
						onClick={() => setEditing(true)}
					>
						Follow up
					</button>
				)}
			</div>
		</div>
	);
}

export default Reports;
