// src/pages/users/UsersPage.jsx
import { useCallback, useEffect, useState } from 'react';
import { userService } from '../../services/userService';
import UserDetailsModal from '../../components/users/UserDetailsModal';
import '../../styles/UsersPage.css';

const ROLES = ['ADMIN', 'MANAGER', 'DEVELOPER'];

export default function UsersPage() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedUser, setSelectedUser] = useState(null);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  const fetchUsers = useCallback(async () => {
    try {
      setLoading(true);
      const res = await userService.getAll({ page, size });
      const payload = res.data;
      const rows = Array.isArray(payload)
        ? payload
        : Array.isArray(payload?.content)
          ? payload.content
          : [];

      setUsers(rows);
      if (Array.isArray(payload)) {
        setTotalElements(rows.length);
        setTotalPages(1);
      } else {
        setTotalElements(Number(payload?.totalElements ?? rows.length));
        setTotalPages(Math.max(1, Number(payload?.totalPages ?? 1)));
      }
      setError('');
    } catch (err) {
      console.error(err);
      setError('Failed to load users.');
    } finally {
      setLoading(false);
    }
  }, [page, size]);

  useEffect(() => { fetchUsers(); }, [fetchUsers]);

  const handleRoleChange = async (userId, newRole) => {
    try {
      await userService.changeRole(userId, newRole);
      setUsers(prev => prev.map(u => u.id === userId ? { ...u, role: newRole } : u));
    } catch (err) {
      console.error(err);
      alert('Failed to update role.');
    }
  };

  const handleDelete = async (userId) => {
    if (!window.confirm('Delete this user?')) return;
    try {
      await userService.delete(userId);
      setUsers(prev => prev.filter(u => u.id !== userId));
      setTotalElements(prev => Math.max(0, prev - 1));
    } catch (err) {
      console.error(err);
      alert('Failed to delete user.');
    }
  };

  if (loading) return <div className="users-page"><div className="users-loading">Loading users…</div></div>;
  if (error)   return <div className="users-page"><div className="users-error">{error}</div></div>;

  return (
    <div className="users-page">
      <div className="users-container">

        {/* Header */}
        <header className="users-header">
          <div>
            <p className="users-kicker">Administration</p>
            <h2>User Management</h2>
            <p className="users-subtitle">{totalElements} registered users</p>
          </div>
        </header>

        {/* Table */}
        <section className="users-panel">
          <div className="users-table">
            <div className="users-table-header users-table-row">
              <div className="users-table-cell">Name</div>
              <div className="users-table-cell">Email</div>
              <div className="users-table-cell">Role</div>
              <div className="users-table-cell">Actions</div>
            </div>

            {users.length === 0 && (
              <div className="users-empty">No users found.</div>
            )}

            {users.map(user => (
              <div key={user.id} className="users-table-row users-data-row">
                {/* Name */}
                <div className="users-table-cell">
                  <div className="users-name">{user.name}</div>
                </div>

                {/* Email */}
                <div className="users-table-cell users-email">{user.email}</div>

                {/* Role */}
                <div className="users-table-cell">
                  <select
                    value={user.role}
                    onChange={(e) => handleRoleChange(user.id, e.target.value)}
                    className={`users-role-badge users-role-${user.role?.toLowerCase()}`}
                  >
                    {ROLES.map(role => (
                      <option key={role} value={role}>{role}</option>
                    ))}
                  </select>
                </div>

                {/* Actions */}
                <div className="users-table-cell users-actions-cell">
                  <button
                    type="button"
                    className="users-btn users-btn--view"
                    onClick={() => setSelectedUser(user)}
                  >
                    View
                  </button>
                  <button
                    type="button"
                    className="users-btn users-btn--delete"
                    onClick={() => handleDelete(user.id)}
                  >
                    Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        </section>

        {/* Pagination */}
        <div className="users-pagination">
          <span className="users-page-info">
            Page {totalPages === 0 ? 0 : page + 1} of {totalPages}
          </span>
          <div className="users-page-controls">
            <label htmlFor="users-page-size" className="users-page-label">Rows:</label>
            <select
              id="users-page-size"
              className="users-page-select"
              value={size}
              onChange={(e) => { setPage(0); setSize(Number(e.target.value)); }}
            >
              <option value={5}>5</option>
              <option value={10}>10</option>
              <option value={20}>20</option>
              <option value={50}>50</option>
            </select>
            <button
              type="button"
              className="users-page-btn"
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              disabled={page === 0}
            >
              Prev
            </button>
            <button
              type="button"
              className="users-page-btn"
              onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
              disabled={page >= totalPages - 1}
            >
              Next
            </button>
          </div>
        </div>
      </div>

      {selectedUser && (
        <UserDetailsModal user={selectedUser} onClose={() => setSelectedUser(null)} />
      )}
    </div>
  );
}
