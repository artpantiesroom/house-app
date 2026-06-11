import { apiRequest } from './apiClient.js';

export const apartmentsApi = {
  list: () => apiRequest('/admin/apartments'),
  create: (payload) => apiRequest('/admin/apartments', { method: 'POST', body: payload }),
  get: (id) => apiRequest(`/admin/apartments/${id}`),
  update: (id, payload) => apiRequest(`/admin/apartments/${id}`, { method: 'PUT', body: payload }),
  remove: (id) => apiRequest(`/admin/apartments/${id}`, { method: 'DELETE' }),
};
