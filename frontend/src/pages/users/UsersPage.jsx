// src/pages/users/UsersPage.jsx

import { useCallback, useEffect, useState } from 'react';
import { userService } from '../../services/userService';
import UserDetailsModal from '../../components/users/UserDetailsModal';

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
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // Fetch users
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

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

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
      setTotalElements(prev => Math.max(0, prev - 1));
    } catch (err) {
      console.error(err);
      alert('Failed to delete user.');
    }
  };

  // Loading state
  if (loading) {
    return (
      <div className="p-8 text-center text-gray-500">
        Loading users...
      </div>
    );
  }

  // Error state
  if (error) {
    return (
      <div className="p-8 text-center text-red-500">
        {error}
      </div>
    );
  }

  return (
    <div className="p-6">
      {/* Title */}
      <h1 className="text-2xl font-bold mb-6 text-gray-800">
        User Management
      </h1>
      <p className="text-sm text-gray-500 mb-4">{totalElements} users total</p>

      {/* Table */}
      <div className="overflow-x-auto rounded-xl shadow border border-gray-200">
        <table className="min-w-full bg-white text-sm">
          {/* Header */}
          <thead className="bg-gray-50 text-gray-600 uppercase text-xs">
            <tr>
              <th className="px-4 py-3 text-left">Name</th>
              <th className="px-4 py-3 text-left">Email</th>
              <th className="px-4 py-3 text-left">Role</th>
              <th className="px-4 py-3 text-left">Actions</th>
            </tr>
          </thead>

          {/* Body */}
          <tbody className="divide-y divide-gray-100">
            {users.map(user => (
              <tr
                key={user.id}
                className="hover:bg-gray-50 transition"
              >
                {/* Name */}
                <td className="px-4 py-3 font-medium text-gray-800">
                  {user.name}
                </td>

                {/* Email */}
                <td className="px-4 py-3 text-gray-600">
                  {user.email}
                </td>

                {/* Role */}
                <td className="px-4 py-3">
                  <select
                    value={user.role}
                    onChange={(e) =>
                      handleRoleChange(user.id, e.target.value)
                    }
                    className={`text-xs font-semibold px-2 py-1 rounded-full border-0 cursor-pointer
                      ${ROLE_COLORS[user.role]}
                      focus:ring-2 focus:ring-blue-400`}
                  >
                    {ROLES.map(role => (
                      <option key={role} value={role}>
                        {role}
                      </option>
                    ))}
                  </select>
                </td>

                {/* Actions */}
                <td className="px-4 py-3 flex gap-3">
                  <button
                    onClick={() => setSelectedUser(user)}
                    className="text-blue-600 hover:underline text-xs"
                  >
                    View
                  </button>

                  <button
                    onClick={() => handleDelete(user.id)}
                    className="text-red-500 hover:underline text-xs"
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="mt-4 flex flex-wrap items-center justify-between gap-3 text-sm text-gray-600">
        <div>Page {totalPages === 0 ? 0 : page + 1} of {totalPages}</div>
        <div className="flex items-center gap-2">
          <label htmlFor="users-page-size">Rows:</label>
          <select
            id="users-page-size"
            className="border rounded px-2 py-1"
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
          <button
            type="button"
            className="border rounded px-3 py-1 disabled:opacity-50"
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            disabled={page === 0}
          >
            Prev
          </button>
          <button
            type="button"
            className="border rounded px-3 py-1 disabled:opacity-50"
            onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
            disabled={page >= totalPages - 1}
          >
            Next
          </button>
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
