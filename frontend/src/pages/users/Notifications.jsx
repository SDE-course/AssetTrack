import React, {useEffect, useMemo, useState} from 'react';
import '../../styles/notifications.css';

function Notifications({onNavigate}) {
	const [activeFilter, setActiveFilter] = useState('all');
	const [notifications, setNotifications] = useState([]);
	const [loading, setLoading] = useState(true);
	const [error, setError] = useState(null);

	const filters = [
		{ key: 'all', label: 'All' },
		{ key: 'critical', label: 'Critical' },
		{ key: 'assignment', label: 'Assignment' },
		{ key: 'warning', label: 'Warning' },
		{ key: 'success', label: 'Success' },
		{ key: 'info', label: 'Info' },
	];

	useEffect(() => {
		async function loadNotifications() {
			try {
				const res = await fetch('/api/notifications');
				if (!res.ok) {
					throw new Error('Failed to load notifications');
				}
				const data = await res.json();
				setNotifications(Array.isArray(data) ? data : []);
			} catch (e) {
				setError(e.message || 'Error');
			} finally {
				setLoading(false);
			}
		}

		loadNotifications();
	}, []);

	const filteredNotifications = useMemo(() => {
		if (activeFilter === 'all') {
			return notifications;
		}
		return notifications.filter((notification) => notification.category === activeFilter);
	}, [activeFilter, notifications]);

	const unreadCount = notifications.filter((notification) => notification.unread).length;
	const criticalCount = notifications.filter((notification) => notification.category === 'critical').length;
	const assignmentCount = notifications.filter((notification) => notification.category === 'assignment').length;

	async function markAllAsRead() {
		await fetch('/api/notifications/mark-all-read', { method: 'POST' });
		setNotifications((current) => current.map((notification) => ({ ...notification, unread: false })));
	}

	async function markRead(id) {
		await fetch(`/api/notifications/${id}/read`, { method: 'POST' });
		setNotifications((current) => current.map((notification) => (notification.id === id ? { ...notification, unread: false } : notification)));
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
							<span>Critical</span>
							<strong>{criticalCount}</strong>
						</div>
						<div className="notifications-stat-card">
							<span>Assignments</span>
							<strong>{assignmentCount}</strong>
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
							<span>{filteredNotifications.length} items</span>
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
										<button type="button" onClick={() => markRead(notification.id)}>Dismiss</button>
									</div>
								</article>
							))}

							{filteredNotifications.length === 0 && (
								<div className="notifications-empty-state">No notifications found.</div>
							)}
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
