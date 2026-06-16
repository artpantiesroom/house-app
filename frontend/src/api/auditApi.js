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

export const auditApi = {
  list: (filters) => apiRequest(`/admin/audit-logs${toQuery(filters)}`),
  get: (id) => apiRequest(`/admin/audit-logs/${id}`),
};
