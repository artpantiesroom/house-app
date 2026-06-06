import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import { demoUsers, simulateRequest } from '../data/mockData.js';
import { useAudit } from './AuditContext.jsx';

const AuthContext = createContext(null);
const SESSION_KEY = 'house_session';
const EXPIRY_MS = 1000 * 60 * 60 * 24 * 7;
const INACTIVITY_MS = 1000 * 60 * 15;
const WARNING_MS = 1000 * 60;
const DEMO_CREDENTIALS = {
  'admin@house.com': 'Admin123!',
  'resident@house.com': 'Resident123!',
};
const SESSION_FIELDS = ['id', 'name', 'email', 'role', 'lastLoginTime'];

const safeUser = (user) => ({
  id: user.id,
  residentId: user.residentId,
  name: user.name,
  email: user.email,
  role: user.role,
  lastLoginTime: new Date().toISOString(),
});

const getStoredSession = () => {
  const sessionValue = sessionStorage.getItem(SESSION_KEY);
  if (sessionValue) return JSON.parse(sessionValue);
  const localValue = localStorage.getItem(SESSION_KEY);
  if (!localValue) return null;
  const parsed = JSON.parse(localValue);
  if (parsed.expiresAt && parsed.expiresAt < Date.now()) {
    localStorage.removeItem(SESSION_KEY);
    return null;
  }
  return parsed;
};

const isSafeStoredUser = (storedUser) => {
  if (!storedUser || typeof storedUser !== 'object') return false;
  if ('password' in storedUser) return false;
  if (!['Administrator', 'Resident'].includes(storedUser.role)) return false;
  if (storedUser.role === 'Resident' && !storedUser.residentId) return false;
  return SESSION_FIELDS.every((field) => typeof storedUser[field] === 'string' && storedUser[field].length > 0);
};

export function AuthProvider({ children }) {
  const { appendAuditLog } = useAudit();
  const [user, setUser] = useState(null);
  const [showTimeoutWarning, setShowTimeoutWarning] = useState(false);
  const [secondsLeft, setSecondsLeft] = useState(60);
  const warningTimer = useRef(null);
  const logoutTimer = useRef(null);
  const countdownTimer = useRef(null);

  useEffect(() => {
    try {
      const saved = getStoredSession();
      if (isSafeStoredUser(saved?.user)) {
        setUser(saved.user);
      } else {
        sessionStorage.removeItem(SESSION_KEY);
        localStorage.removeItem(SESSION_KEY);
      }
    } catch {
      sessionStorage.removeItem(SESSION_KEY);
      localStorage.removeItem(SESSION_KEY);
    }
  }, []);

  const clearTimers = useCallback(() => {
    window.clearTimeout(warningTimer.current);
    window.clearTimeout(logoutTimer.current);
    window.clearInterval(countdownTimer.current);
  }, []);

  const storeSession = (nextUser, remember) => {
    const payload = remember ? { user: nextUser, expiresAt: Date.now() + EXPIRY_MS } : { user: nextUser };
    sessionStorage.removeItem(SESSION_KEY);
    localStorage.removeItem(SESSION_KEY);
    if (remember) localStorage.setItem(SESSION_KEY, JSON.stringify(payload));
    else sessionStorage.setItem(SESSION_KEY, JSON.stringify(payload));
  };

  const logout = useCallback((reason = 'USER_LOGOUT', actorOverride) => {
    const actor = actorOverride || user?.email || 'unknown@house.com';
    clearTimers();
    sessionStorage.removeItem(SESSION_KEY);
    localStorage.removeItem(SESSION_KEY);
    setShowTimeoutWarning(false);
    setUser(null);
    appendAuditLog({ actor, action: 'LOGOUT', target: reason, result: 'SUCCESS' });
  }, [appendAuditLog, clearTimers, user]);

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
    await simulateRequest(null, 1200);
    const matched = demoUsers.find((candidate) => candidate.email === email && DEMO_CREDENTIALS[candidate.email] === password);
    if (!matched) {
      appendAuditLog({ actor: email || 'unknown@house.com', action: 'LOGIN', target: 'Auth', result: 'FAILED' });
      throw new Error('Неправильний email або пароль.');
    }
    const nextUser = safeUser(matched);
    storeSession(nextUser, remember);
    setUser(nextUser);
    appendAuditLog({ actor: nextUser.email, action: 'LOGIN', target: 'Auth', result: 'SUCCESS' });
    return nextUser;
  }, [appendAuditLog]);

  const value = useMemo(() => ({ user, login, logout, resetActivityTimers, showTimeoutWarning, secondsLeft }), [user, login, logout, resetActivityTimers, showTimeoutWarning, secondsLeft]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);
