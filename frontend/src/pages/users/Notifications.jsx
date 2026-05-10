import React, {useEffect, useMemo, useState} from 'react';
import '../../styles/notifications.css';

function Notifications({onNavigate}) {
	const [activeFilter, setActiveFilter] = useState('all');
	const [notifications, setNotifications] = useState([]);
	const [loading, setLoading] = useState(true);
	const [error, setError] = useState(null);
	const [page, setPage] = useState(0);
	const [size, setSize] = useState(10);
	const [totalPages, setTotalPages] = useState(1);
	const [totalElements, setTotalElements] = useState(0);

	const filters = [
		{ key: 'all', label: 'All' },
		{ key: 'warranty', label: 'Warranty' },
		{ key: 'low-stock', label: 'Low Stock' },
		{ key: 'assignment', label: 'Assignment' },
	];

	useEffect(() => {
		async function loadNotifications() {
			try {
				const token = localStorage.getItem('token');
				const res = await fetch(`/api/notifications?page=${page}&size=${size}`, {
					headers: {
						Authorization: token ? `Bearer ${token}` : undefined,
					},
				});
				if (!res.ok) {
					throw new Error('Failed to load notifications');
				}
				const data = await res.json();
				const rows = Array.isArray(data)
					? data
					: Array.isArray(data?.content)
						? data.content
						: [];
				setNotifications(rows);

				if (Array.isArray(data)) {
					setTotalElements(rows.length);
					setTotalPages(1);
				} else {
					setTotalElements(Number(data?.totalElements ?? rows.length));
					setTotalPages(Math.max(1, Number(data?.totalPages ?? 1)));
				}
			} catch (e) {
				setError(e.message || 'Error');
			} finally {
				setLoading(false);
			}
		}

		loadNotifications();
	}, [page, size]);

	const filteredNotifications = useMemo(() => {
		if (activeFilter === 'all') {
			return notifications;
		}
		return notifications.filter((notification) => notification.category === activeFilter);
	}, [activeFilter, notifications]);

	const unreadCount = notifications.filter((notification) => notification.unread).length;
	const warrantyCount = notifications.filter((notification) => notification.category === 'warranty').length;
	const lowStockCount = notifications.filter((notification) => notification.category === 'low-stock').length;

	async function markAllAsRead() {
		const token = localStorage.getItem('token');
		await fetch('/api/notifications/mark-all-read', { 
			method: 'POST',
			headers: { Authorization: token ? `Bearer ${token}` : undefined }
		});
		setNotifications((current) => current.map((notification) => ({ ...notification, unread: false })));
	}

	async function markRead(id) {
		const token = localStorage.getItem('token');
		await fetch(`/api/notifications/${id}/read`, { 
			method: 'POST',
			headers: { Authorization: token ? `Bearer ${token}` : undefined }
		});
		setNotifications((current) => current.map((notification) => (notification.id === id ? { ...notification, unread: false } : notification)));
	}

	async function deleteNotification(id) {
		const token = localStorage.getItem('token');
		try {
			const res = await fetch(`/api/notifications/${id}`, {
				method: 'DELETE',
				headers: { Authorization: token ? `Bearer ${token}` : undefined }
			});
			if (!res.ok) throw new Error('Failed to delete notification');
			setNotifications((current) => current.filter((n) => n.id !== id));
			setTotalElements((current) => Math.max(0, current - 1));
		} catch (e) {
			console.error(e);
			alert(e.message || 'Failed to delete notification');
		}
	}

	if (loading) {
		return <div className="notifications-page">Loading notifications...</div>;
	}

	if (error) {
		return <div className="notifications-page">Error: {error}</div>;
	}

	return (
		<div className="notifications-page">
			<div className="notifications-shell">
				<header className="notifications-header">
					<div>
						<p className="notifications-kicker">Activity center</p>
						<h2>Notifications</h2>
						<p className="notifications-subtitle">
							Track asset alerts, assignment updates, returns, and warranty reminders in one place.
						</p>
					</div>

					<div className="notifications-quick-stats">
						<div className="notifications-stat-card">
							<span>Unread</span>
							<strong>{unreadCount}</strong>
						</div>
						<div className="notifications-stat-card">
						<span>Warranty</span>
						<strong>{warrantyCount}</strong>
					</div>
					<div className="notifications-stat-card">
						<span>Low Stock</span>
						<strong>{lowStockCount}</strong>
						</div>
					</div>
				</header>

				<div style={{marginBottom: '12px'}}>
					<button type="button" className="notifications-action-button" onClick={() => onNavigate && onNavigate('dashboard')}>
						Back to dashboard
					</button>
				</div>

				<section className="notifications-toolbar">
					<div className="notifications-filter-group">
						{filters.map((filter) => (
							<button
								key={filter.key}
								type="button"
								className={`notifications-filter ${activeFilter === filter.key ? 'is-active' : ''}`}
								onClick={() => setActiveFilter(filter.key)}
							>
								{filter.label}
							</button>
						))}
					</div>

					<button type="button" className="notifications-action-button" onClick={markAllAsRead}>
						Mark all as read
					</button>
				</section>

				<main className="notifications-layout">
					<section className="notifications-list-panel">
						<div className="notifications-panel-header">
							<h3>Recent alerts</h3>
							<span>{totalElements} total</span>
						</div>

						<div className="notifications-list">
							{filteredNotifications.map((notification) => (
								<article
									key={notification.id}
									className={`notification-card ${notification.unread ? 'is-unread' : ''}`}
								>
									<div className={`notification-icon category-${notification.category}`}>
										{notification.category === 'critical' && '!'}
										{notification.category === 'assignment' && 'A'}
										{notification.category === 'warning' && 'W'}
									{notification.category === 'warranty' && '⏰'}
									{notification.category === 'low-stock' && '📦'}
									{notification.category === 'success' && '✓'}
									{notification.category === 'info' && 'i'}
								</div>

								<div className="notification-content">
										<div className="notification-title-row">
											<h4>{notification.title}</h4>
											{notification.unread && <span className="notification-dot" />}
										</div>
										<p>{notification.message}</p>
										<div className="notification-meta">
											<span>{notification.assetTag || 'General'}</span>
											<span>{notification.createdAt ? new Date(notification.createdAt).toLocaleString() : 'Recently'}</span>
										</div>
									</div>

									<div className="notification-actions">
										<button type="button" onClick={() => markRead(notification.id)}>View</button>
										<button type="button" onClick={() => deleteNotification(notification.id)}>Dismiss</button>
									</div>
								</article>
							))}

							{filteredNotifications.length === 0 && (
								<div className="notifications-empty-state">No notifications found.</div>
							)}
						</div>

						<div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 12, gap: 12, flexWrap: 'wrap' }}>
							<div style={{ color: '#94a3b8', fontSize: 13 }}>
								Page {totalPages === 0 ? 0 : page + 1} of {totalPages}
							</div>
							<div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
								<label htmlFor="notifications-page-size" style={{ fontSize: 13, color: '#94a3b8' }}>Rows:</label>
								<select
									id="notifications-page-size"
									value={size}
									onChange={(e) => {
										setPage(0);
										setSize(Number(e.target.value));
									}}
								>
									<option value={5}>5</option>
									<option value={10}>10</option>
									<option value={20}>20</option>
									<option value={50}>50</option>
								</select>
								<button type="button" className="notifications-action-button" onClick={() => setPage((p) => Math.max(0, p - 1))} disabled={page === 0}>
									Prev
								</button>
								<button type="button" className="notifications-action-button" onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))} disabled={page >= totalPages - 1}>
									Next
								</button>
							</div>
						</div>
					</section>

					<aside className="notifications-side-panel">
						<div className="notifications-side-card">
							<h3>Summary</h3>
							<div className="notifications-summary-row">
								<span>Total alerts</span>
								<strong>{notifications.length}</strong>
							</div>
							<div className="notifications-summary-row">
								<span>Unread alerts</span>
								<strong>{unreadCount}</strong>
							</div>
							<div className="notifications-summary-row">
								<span>Most urgent</span>
								<strong>Warranty</strong>
							</div>
						</div>

						<div className="notifications-side-card">
							<h3>Today’s focus</h3>
							<ul className="notifications-focus-list">
								<li>Review assets with expiring warranties.</li>
								<li>Check newly assigned laptops for delivery.</li>
								<li>Confirm spare laptop availability for requests.</li>
							</ul>
						</div>
					</aside>
				</main>
			</div>
		</div>
	);
}

export default Notifications;
