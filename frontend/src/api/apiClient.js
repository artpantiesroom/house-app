export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

let accessToken = null;
let refreshTokens = null;
let handleRefreshFailure = null;

export const setAccessToken = (token) => {
  accessToken = token || null;
};

export const setRefreshTokensHandler = (handler) => {
  refreshTokens = handler;
};

export const setRefreshFailureHandler = (handler) => {
  handleRefreshFailure = handler;
};

export async function apiRequest(path, options = {}) {
  return request(path, options, false);
}

async function request(path, options, didRetry) {
  const { body, auth = true, skipRefresh = false, headers = {}, responseType = 'json', ...fetchOptions } = options;
  const isFormData = body instanceof FormData;
  const requestHeaders = {
    ...headers,
  };

  if (body !== undefined && !isFormData) {
    requestHeaders['Content-Type'] = 'application/json';
  }
  if (auth && accessToken) {
    requestHeaders.Authorization = `Bearer ${accessToken}`;
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...fetchOptions,
    headers: requestHeaders,
    body: body === undefined ? undefined : isFormData ? body : JSON.stringify(body),
  });

  if (response.status === 401 && auth && !skipRefresh && !didRetry && refreshTokens) {
    try {
      await refreshTokens();
      return request(path, options, true);
    } catch (error) {
      if (handleRefreshFailure) handleRefreshFailure();
      throw error;
    }
  }

  if (!response.ok) {
    throw await toApiError(response);
  }

  if (response.status === 204) {
    return null;
  }

  if (responseType === 'blob') {
    return response.blob();
  }

  return response.json();
}

async function toApiError(response) {
  try {
    const payload = await response.json();
    const error = new Error(payload.message || 'Запит не вдалося виконати.');
    error.status = response.status;
    return error;
  } catch {
    const error = new Error('Запит не вдалося виконати.');
    error.status = response.status;
    return error;
  }
}
