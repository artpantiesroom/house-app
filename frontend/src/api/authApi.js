import { apiRequest } from './apiClient.js';

export const authApi = {
  login: ({ email, password, rememberMe }) => apiRequest('/auth/login', {
    method: 'POST',
    auth: false,
    skipRefresh: true,
    body: { email, password, rememberMe },
  }),

  refresh: (refreshToken) => apiRequest('/auth/refresh', {
    method: 'POST',
    auth: false,
    skipRefresh: true,
    body: { refreshToken },
  }),

  logout: (refreshToken) => apiRequest('/auth/logout', {
    method: 'POST',
    auth: false,
    skipRefresh: true,
    body: { refreshToken },
  }),

  changePassword: ({ currentPassword, newPassword }) => apiRequest('/auth/change-password', {
    method: 'POST',
    body: { currentPassword, newPassword },
  }),

  me: () => apiRequest('/auth/me'),
};
