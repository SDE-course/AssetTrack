// src/services/userService.js
// All API calls for Users Management (Member 2)
// Assumes axios instance with JWT interceptor set up by Member 1

import api from './api'; // axios instance from Member 1

export const userService = {

  // Get all users
  getAll: (params = {}) => api.get('/users', { params }),

  // Get single user
  getById: (id) => api.get(`/users/${id}`),

  // Get users filtered by role
  getByRole: (role) => api.get(`/users/by-role?role=${role}`),

  // Create user (Admin only)
  create: (data) => api.post('/users', data),

  // Update user info (Admin only)
  update: (id, data) => api.put(`/users/${id}`, data),

  // Change role (Admin only)
  changeRole: (id, role) => api.patch(`/users/${id}/role`, { role }),

  // Delete (soft delete) user (Admin only)
  delete: (id) => api.delete(`/users/${id}`),
};
