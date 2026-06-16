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

export const incidentsApi = {
  list: (filters) => apiRequest(`/admin/security-incidents${toQuery(filters)}`),
  create: (payload) => apiRequest('/admin/security-incidents', { method: 'POST', body: payload }),
  get: (id) => apiRequest(`/admin/security-incidents/${id}`),
  update: (id, payload) => apiRequest(`/admin/security-incidents/${id}`, { method: 'PUT', body: payload }),
  updateStatus: (id, payload) => apiRequest(`/admin/security-incidents/${id}/status`, { method: 'PATCH', body: payload }),
  softDelete: (id) => apiRequest(`/admin/security-incidents/${id}`, { method: 'DELETE' }),
};
