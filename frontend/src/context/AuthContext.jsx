import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import { authApi } from '../api/authApi.js';
import { setAccessToken, setRefreshFailureHandler, setRefreshTokensHandler } from '../api/apiClient.js';
import { useAudit } from './AuditContext.jsx';

const AuthContext = createContext(null);
const REFRESH_TOKEN_KEY = 'house_refresh_token';
const INACTIVITY_MS = 1000 * 60 * 15;
const WARNING_MS = 1000 * 60;

const normalizeUser = (user) => ({
  id: String(user.id),
  residentId: user.residentId || (user.email === 'resident@house.com' ? 'res-1' : null),
  name: user.name,
  email: user.email,
  role: user.role,
  mustChangePassword: user.mustChangePassword,
  preferredLanguage: user.preferredLanguage || 'uk',
  lastLoginTime: user.lastLoginAt || new Date().toISOString(),
});

const storeRefreshToken = (refreshToken) => {
  if (refreshToken) localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
};

const getRefreshToken = () => localStorage.getItem(REFRESH_TOKEN_KEY);

const clearRefreshToken = () => {
  localStorage.removeItem(REFRESH_TOKEN_KEY);
};

export function AuthProvider({ children }) {
  const { appendAuditLog } = useAudit();
  const [user, setUser] = useState(null);
  const [authReady, setAuthReady] = useState(false);
  const [showTimeoutWarning, setShowTimeoutWarning] = useState(false);
  const [secondsLeft, setSecondsLeft] = useState(60);
  const warningTimer = useRef(null);
  const logoutTimer = useRef(null);
  const countdownTimer = useRef(null);

  const clearTimers = useCallback(() => {
    window.clearTimeout(warningTimer.current);
    window.clearTimeout(logoutTimer.current);
    window.clearInterval(countdownTimer.current);
  }, []);

  const clearSession = useCallback(() => {
    clearTimers();
    setAccessToken(null);
    clearRefreshToken();
    setShowTimeoutWarning(false);
    setUser(null);
  }, [clearTimers]);

  const applyAuthResponse = useCallback((response) => {
    setAccessToken(response.accessToken);
    storeRefreshToken(response.refreshToken);
    const nextUser = normalizeUser(response.user);
    setUser(nextUser);
    return nextUser;
  }, []);

  const refreshSession = useCallback(async () => {
    const refreshToken = getRefreshToken();
    if (!refreshToken) {
      throw new Error('Сеанс не знайдено.');
    }
    const response = await authApi.refresh(refreshToken);
    return applyAuthResponse(response);
  }, [applyAuthResponse]);

  useEffect(() => {
    setRefreshTokensHandler(refreshSession);
    setRefreshFailureHandler(clearSession);
  }, [clearSession, refreshSession]);

  useEffect(() => {
    let active = true;
    const restore = async () => {
      const refreshToken = getRefreshToken();
      if (!refreshToken) {
        if (active) setAuthReady(true);
        return;
      }
      try {
        await refreshSession();
      } catch {
        clearSession();
      } finally {
        if (active) setAuthReady(true);
      }
    };
    restore();
    return () => {
      active = false;
    };
  }, [clearSession, refreshSession]);

  const logout = useCallback(async (reason = 'USER_LOGOUT', actorOverride) => {
    const actor = actorOverride || user?.email || 'unknown@house.com';
    const refreshToken = getRefreshToken();
    clearSession();
    if (refreshToken) {
      try {
        await authApi.logout(refreshToken);
      } catch {
        // Local logout must still complete when the server session is already gone.
      }
    }
    appendAuditLog({ actor, action: 'LOGOUT', target: reason, result: 'SUCCESS' });
  }, [appendAuditLog, clearSession, user]);

  const resetActivityTimers = useCallback(() => {
    if (!user) return;
    const actor = user.email;
    clearTimers();
    setShowTimeoutWarning(false);
    warningTimer.current = window.setTimeout(() => {
      setSecondsLeft(60);
      setShowTimeoutWarning(true);
      countdownTimer.current = window.setInterval(() => {
        setSecondsLeft((value) => Math.max(value - 1, 0));
      }, 1000);
    }, INACTIVITY_MS - WARNING_MS);
    logoutTimer.current = window.setTimeout(() => logout('INACTIVITY_TIMEOUT', actor), INACTIVITY_MS);
  }, [clearTimers, logout, user]);

  useEffect(() => {
    if (!user) return undefined;
    const events = ['mousemove', 'keydown', 'click', 'scroll'];
    events.forEach((event) => window.addEventListener(event, resetActivityTimers, { passive: true }));
    resetActivityTimers();
    return () => {
      events.forEach((event) => window.removeEventListener(event, resetActivityTimers));
      clearTimers();
    };
  }, [clearTimers, resetActivityTimers, user]);

  const login = useCallback(async ({ email, password, remember }) => {
    try {
      const response = await authApi.login({ email, password, rememberMe: remember });
      const nextUser = applyAuthResponse(response);
      appendAuditLog({ actor: nextUser.email, action: 'LOGIN', target: 'Auth', result: 'SUCCESS' });
      return nextUser;
    } catch (error) {
      appendAuditLog({ actor: email || 'unknown@house.com', action: 'LOGIN', target: 'Auth', result: 'FAILED' });
      throw new Error(error.message || 'Неправильний email або пароль.');
    }
  }, [appendAuditLog, applyAuthResponse]);

  const changePassword = useCallback(async ({ currentPassword, newPassword }) => {
    const response = await authApi.changePassword({ currentPassword, newPassword });
    const nextUser = applyAuthResponse(response);
    appendAuditLog({ actor: nextUser.email, action: 'PASSWORD_CHANGED', target: 'Auth', result: 'SUCCESS' });
    return nextUser;
  }, [appendAuditLog, applyAuthResponse]);

  const value = useMemo(() => ({
    user,
    authReady,
    login,
    logout,
    changePassword,
    resetActivityTimers,
    showTimeoutWarning,
    secondsLeft,
  }), [user, authReady, login, logout, changePassword, resetActivityTimers, showTimeoutWarning, secondsLeft]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);
