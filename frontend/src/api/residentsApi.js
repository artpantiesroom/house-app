import { apiRequest } from './apiClient.js';

export const residentsApi = {
  list: () => apiRequest('/admin/residents'),
  create: (payload) => apiRequest('/admin/residents', { method: 'POST', body: payload }),
  get: (id) => apiRequest(`/admin/residents/${id}`),
  update: (id, payload) => apiRequest(`/admin/residents/${id}`, { method: 'PUT', body: payload }),
  deactivate: (id) => apiRequest(`/admin/residents/${id}`, { method: 'DELETE' }),
  getOwnProfile: () => apiRequest('/resident/profile'),
  updateOwnProfile: (payload) => apiRequest('/resident/profile', { method: 'PUT', body: payload }),
};
