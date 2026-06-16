import { apiRequest } from './apiClient.js';

export const contactsApi = {
  listAdmin: () => apiRequest('/admin/contacts'),
  create: (payload) => apiRequest('/admin/contacts', { method: 'POST', body: payload }),
  getAdmin: (id) => apiRequest(`/admin/contacts/${id}`),
  update: (id, payload) => apiRequest(`/admin/contacts/${id}`, { method: 'PUT', body: payload }),
  deactivate: (id) => apiRequest(`/admin/contacts/${id}`, { method: 'DELETE' }),
  listResident: () => apiRequest('/resident/contacts'),
};
