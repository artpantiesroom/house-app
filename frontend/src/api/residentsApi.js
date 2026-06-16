import { apiRequest } from './apiClient.js';

const formData = (file) => {
  const data = new FormData();
  data.append('file', file);
  return data;
};

export const residentsApi = {
  list: () => apiRequest('/admin/residents'),
  create: (payload) => apiRequest('/admin/residents', { method: 'POST', body: payload }),
  get: (id) => apiRequest(`/admin/residents/${id}`),
  update: (id, payload) => apiRequest(`/admin/residents/${id}`, { method: 'PUT', body: payload }),
  deactivate: (id) => apiRequest(`/admin/residents/${id}`, { method: 'DELETE' }),
  uploadAvatar: (id, file) => apiRequest(`/admin/residents/${id}/avatar`, { method: 'POST', body: formData(file) }),
  deleteAvatar: (id) => apiRequest(`/admin/residents/${id}/avatar`, { method: 'DELETE' }),
  getOwnProfile: () => apiRequest('/resident/profile'),
  updateOwnProfile: (payload) => apiRequest('/resident/profile', { method: 'PUT', body: payload }),
  uploadOwnAvatar: (file) => apiRequest('/resident/profile/avatar', { method: 'POST', body: formData(file) }),
  deleteOwnAvatar: () => apiRequest('/resident/profile/avatar', { method: 'DELETE' }),
  fetchAvatar: (avatarUrl) => apiRequest(avatarUrl.replace(/^\/api/, ''), { responseType: 'blob' }),
};
