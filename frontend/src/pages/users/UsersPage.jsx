// src/pages/users/UsersPage.jsx
import { useEffect, useState } from 'react';
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

  const fetchUsers = async () => {
    try {
      setLoading(true);
      const res = await userService.getAll();
      setUsers(res.data);
    } catch {
      setError('Failed to load users.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchUsers(); }, []);

  const handleRoleChange = async (userId, newRole) => {
    try {
      await userService.changeRole(userId, newRole);
      setUsers(prev =>
        prev.map(u => u.id === userId ? { ...u, role: newRole } : u)
      );
    } catch {
      alert('Failed to update role.');
    }
  };

  const handleDelete = async (userId) => {
    if (!window.confirm('Deactivate this user?')) return;
    try {
      await userService.delete(userId);
      setUsers(prev => prev.map(u => u.id === userId ? { ...u, active: false } : u));
    } catch {
      alert('Failed to delete user.');
    }
  };

  if (loading) return <div className="p-8 text-center text-gray-500">Loading users...</div>;
  if (error)   return <div className="p-8 text-center text-red-500">{error}</div>;

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-6 text-gray-800">User Management</h1>

      <div className="overflow-x-auto rounded-xl shadow border border-gray-200">
        <table className="min-w-full bg-white text-sm">
          <thead className="bg-gray-50 text-gray-600 uppercase text-xs">
            <tr>
              <th className="px-4 py-3 text-left">Name</th>
              <th className="px-4 py-3 text-left">Email</th>
              <th className="px-4 py-3 text-left">Role</th>
              <th className="px-4 py-3 text-left">Status</th>
              <th className="px-4 py-3 text-left">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {users.map(user => (
              <tr key={user.id} className="hover:bg-gray-50 transition">
                {/* Name */}
                <td className="px-4 py-3 font-medium text-gray-800">{user.fullName}</td>

                {/* Email */}
                <td className="px-4 py-3 text-gray-600">{user.email}</td>

                {/* Role dropdown */}
                <td className="px-4 py-3">
                  <select
                    value={user.role}
                    onChange={e => handleRoleChange(user.id, e.target.value)}
                    className={`text-xs font-semibold px-2 py-1 rounded-full border-0 cursor-pointer
                      ${ROLE_COLORS[user.role]} focus:ring-2 focus:ring-blue-400`}
                  >
                    {ROLES.map(r => (
                      <option key={r} value={r}>{r}</option>
                    ))}
                  </select>
                </td>

                {/* Status */}
                <td className="px-4 py-3">
                  <span className={`px-2 py-1 rounded-full text-xs font-semibold
                    ${user.active ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
                    {user.active ? 'Active' : 'Inactive'}
                  </span>
                </td>

                {/* Actions */}
                <td className="px-4 py-3 flex gap-2">
                  <button
                    onClick={() => setSelectedUser(user)}
                    className="text-blue-600 hover:underline text-xs"
                  >
                    View
                  </button>
                  {user.active && (
                    <button
                      onClick={() => handleDelete(user.id)}
                      className="text-red-500 hover:underline text-xs"
                    >
                      Deactivate
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* User Details Modal */}
      {selectedUser && (
        <UserDetailsModal
          user={selectedUser}
          onClose={() => setSelectedUser(null)}
        />
      )}
    </div>
  );
}
