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

export const announcementsApi = {
  listAdmin: (filters) => apiRequest(`/admin/announcements${toQuery(filters)}`),
  create: (payload) => apiRequest('/admin/announcements', { method: 'POST', body: payload }),
  getAdmin: (id) => apiRequest(`/admin/announcements/${id}`),
  update: (id, payload) => apiRequest(`/admin/announcements/${id}`, { method: 'PUT', body: payload }),
  archive: (id) => apiRequest(`/admin/announcements/${id}/archive`, { method: 'PATCH' }),
  publish: (id) => apiRequest(`/admin/announcements/${id}/publish`, { method: 'PATCH' }),
  remove: (id) => apiRequest(`/admin/announcements/${id}`, { method: 'DELETE' }),
  listResident: () => apiRequest('/resident/announcements'),
  getResident: (id) => apiRequest(`/resident/announcements/${id}`),
};
