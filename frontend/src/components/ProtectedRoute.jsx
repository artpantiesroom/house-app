import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useEffect, useRef } from 'react';
import { useAuth } from '../context/AuthContext.jsx';
import { useAudit } from '../context/AuditContext.jsx';
import LoadingSpinner from './LoadingSpinner.jsx';

export default function ProtectedRoute({ allowedRoles, allowPasswordChangeRequired = false }) {
  const { user, authReady } = useAuth();
  const { appendAuditLog } = useAudit();
  const location = useLocation();
  const loggedDeniedRef = useRef('');

  useEffect(() => {
    if (user?.role === 'RESIDENT' && location.pathname.startsWith('/admin') && loggedDeniedRef.current !== location.pathname) {
      loggedDeniedRef.current = location.pathname;
      appendAuditLog({ actor: user.email, action: 'FORBIDDEN_ROUTE_ACCESS', target: location.pathname, result: 'DENIED' });
    }
  }, [user, location.pathname, appendAuditLog]);

  if (!authReady) {
    return <main className="grid min-h-screen place-items-center"><LoadingSpinner label="Відновлення сеансу" /></main>;
  }
  if (!user) return <Navigate to="/login" replace state={{ from: location }} />;
  if (user.mustChangePassword && !allowPasswordChangeRequired) {
    return <Navigate to="/change-password" replace state={{ from: location }} />;
  }
  if (!allowedRoles.includes(user.role)) {
    if (user.role === 'ADMIN' && location.pathname.startsWith('/resident')) {
      return <Navigate to="/admin/dashboard" replace />;
    }
    return <Navigate to="/forbidden" replace />;
  }
  return <Outlet />;
}
