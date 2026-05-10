// src/pages/users/UsersPage.jsx

import { useEffect, useState } from 'react';
import { userService } from '../../services/userService';
import UserDetailsModal from '../../components/users/UserDetailsModal';
import '../../styles/UsersPage.css';

const ROLES = ['ADMIN', 'MANAGER', 'DEVELOPER'];

const ROLE_COLORS = {
  ADMIN: 'bg-red-100 text-red-700',
  MANAGER: 'bg-blue-100 text-blue-700',
  DEVELOPER: 'bg-green-100 text-green-700',
};

export default function UsersPage() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedUser, setSelectedUser] = useState(null);

  // Fetch users
  const fetchUsers = async () => {
    try {
      setLoading(true);

      const res = await userService.getAll();

      setUsers(res.data);
      setError('');
    } catch (err) {
      console.error(err);
      setError('Failed to load users.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  // Change role
  const handleRoleChange = async (userId, newRole) => {
    try {
      await userService.changeRole(userId, newRole);

      setUsers(prev =>
        prev.map(user =>
          user.id === userId
            ? { ...user, role: newRole }
            : user
        )
      );
    } catch (err) {
      console.error(err);
      alert('Failed to update role.');
    }
  };

  // Delete user
  const handleDelete = async (userId) => {
    const confirmed = window.confirm('Delete this user?');

    if (!confirmed) return;

    try {
      await userService.delete(userId);

      // Remove user from UI
      setUsers(prev =>
        prev.filter(user => user.id !== userId)
      );
    } catch (err) {
      console.error(err);
      alert('Failed to delete user.');
    }
  };

  // Loading state
  if (loading) {
    return (
      <div className="users-page users-page--centered">
        Loading users...
      </div>
    );
  }

  // Error state
  if (error) {
    return (
      <div className="users-page users-page--centered users-page--error">
        {error}
      </div>
    );
  }

  return (
    <div className="users-page">
      <div className="users-page__header">
        <div>
          <p className="users-page__eyebrow">Admin console</p>
          <h1 className="users-page__title">User Management</h1>
          <p className="users-page__subtitle">
            Manage roles, inspect profiles, and remove inactive accounts.
          </p>
        </div>
        <div className="users-page__summary">
          <span className="users-page__summary-label">Total users</span>
          <span className="users-page__summary-value">{users.length}</span>
        </div>
      </div>

      <div className="users-page__card">
        <div className="users-page__table-wrap">
          <table className="users-page__table">
            <thead>
            <tr>
              <th className="px-4 py-3 text-left">Name</th>
              <th className="px-4 py-3 text-left">Email</th>
              <th className="px-4 py-3 text-left">Role</th>
              <th className="px-4 py-3 text-left">Actions</th>
            </tr>
          </thead>

            <tbody>
              {users.map(user => (
                <tr key={user.id}>
                  <td className="users-page__name">{user.name}</td>

                  <td className="users-page__email">{user.email}</td>

                  <td className="users-page__role-cell">
                    <select
                      value={user.role}
                      onChange={(e) => handleRoleChange(user.id, e.target.value)}
                      className={`users-page__select users-page__select--${user.role.toLowerCase()}`}
                      aria-label={`Change role for ${user.name}`}
                    >
                      {ROLES.map(role => (
                        <option key={role} value={role}>
                          {role}
                        </option>
                      ))}
                    </select>
                  </td>

                  <td className="users-page__actions">
                    <button
                      onClick={() => setSelectedUser(user)}
                      className="users-page__link-btn users-page__link-btn--view"
                    >
                      View
                    </button>

                    <button
                      onClick={() => handleDelete(user.id)}
                      className="users-page__link-btn users-page__link-btn--danger"
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal */}
      {selectedUser && (
        <UserDetailsModal
          user={selectedUser}
          onClose={() => setSelectedUser(null)}
        />
      )}
    </div>
  );
}
