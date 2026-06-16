import { apiRequest } from './apiClient.js';

const toQuery = (filters = {}) => {
  const params = new URLSearchParams();
  Object.entries(filters).forEach(([key, value]) => {
    if (value !== undefined && value !== null && String(value).trim() !== '') {
      params.set(key, value);
    }
  });
  const query = params.toString();
  return query ? `?${query}` : '';
};

export const paymentsApi = {
  listResident: (filters) => apiRequest(`/resident/payments${toQuery(filters)}`),
  getResident: (id) => apiRequest(`/resident/payments/${id}`),
  listAdmin: (filters) => apiRequest(`/admin/payments${toQuery(filters)}`),
  create: (payload) => apiRequest('/admin/payments', { method: 'POST', body: payload }),
  getAdmin: (id) => apiRequest(`/admin/payments/${id}`),
  update: (id, payload) => apiRequest(`/admin/payments/${id}`, { method: 'PUT', body: payload }),
  updateStatus: (id, status) => apiRequest(`/admin/payments/${id}/status`, { method: 'PATCH', body: { status } }),
  cancel: (id) => apiRequest(`/admin/payments/${id}`, { method: 'DELETE' }),
};
