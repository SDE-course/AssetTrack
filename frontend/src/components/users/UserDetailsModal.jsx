// src/components/users/UserDetailsModal.jsx
export default function UserDetailsModal({ user, onClose }) {
  if (!user) return null;

  const fields = [
    { label: 'Full Name', value: user.fullName },
    { label: 'Email',     value: user.email },
    { label: 'Role',      value: user.role },
    { label: 'Status',    value: user.active ? 'Active' : 'Inactive' },
  ];

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-md p-6 relative">
        {/* Close */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-gray-400 hover:text-gray-600 text-xl"
        >
          ✕
        </button>

        {/* Avatar + Name */}
        <div className="flex flex-col items-center mb-6">
          <div className="w-16 h-16 rounded-full bg-blue-100 flex items-center justify-center
                          text-blue-700 text-2xl font-bold mb-2">
            {user.fullName?.charAt(0).toUpperCase()}
          </div>
          <h2 className="text-lg font-bold text-gray-800">{user.fullName}</h2>
          <p className="text-sm text-gray-500">{user.email}</p>
        </div>

        {/* Details */}
        <dl className="divide-y divide-gray-100">
          {fields.map(f => (
            <div key={f.label} className="flex justify-between py-2 text-sm">
              <dt className="text-gray-500 font-medium">{f.label}</dt>
              <dd className="text-gray-800">{f.value}</dd>
            </div>
          ))}
        </dl>

        <button
          onClick={onClose}
          className="mt-6 w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition"
        >
          Close
        </button>
      </div>
    </div>
  );
}
