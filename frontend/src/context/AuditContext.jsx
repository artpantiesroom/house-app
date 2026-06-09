import { createContext, useCallback, useContext, useMemo, useState } from 'react';
import { v4 as uuid } from 'uuid';
import { initialAuditLog } from '../data/mockData.js';

const AuditContext = createContext(null);
const STORAGE_KEY = 'house_audit_log';

const loadAuditLog = () => {
  try {
    const saved = sessionStorage.getItem(STORAGE_KEY);
    return saved ? JSON.parse(saved) : initialAuditLog;
  } catch {
    return initialAuditLog;
  }
};

export function AuditProvider({ children }) {
  const [auditLog, setAuditLog] = useState(loadAuditLog);

  const appendAuditLog = useCallback(({ actor = 'unknown@house.com', action, target, result = 'SUCCESS' }) => {
    const entry = {
      id: uuid(),
      timestamp: new Date().toISOString(),
      actor,
      action,
      target,
      result,
    };
    setAuditLog((current) => {
      const next = [entry, ...current];
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(next));
      return next;
    });
    return entry;
  }, []);

  const value = useMemo(() => ({ auditLog, appendAuditLog }), [auditLog]);
  return <AuditContext.Provider value={value}>{children}</AuditContext.Provider>;
}

export const useAudit = () => useContext(AuditContext);
