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

export const maintenanceApi = {
  listResident: () => apiRequest('/resident/maintenance-requests'),
  createResident: (payload) => apiRequest('/resident/maintenance-requests', { method: 'POST', body: payload }),
  getResident: (id) => apiRequest(`/resident/maintenance-requests/${id}`),
  listAdmin: (filters) => apiRequest(`/admin/maintenance-requests${toQuery(filters)}`),
  getAdmin: (id) => apiRequest(`/admin/maintenance-requests/${id}`),
  updateAdmin: (id, payload) => apiRequest(`/admin/maintenance-requests/${id}`, { method: 'PATCH', body: payload }),
};
