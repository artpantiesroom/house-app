import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useEffect, useRef } from 'react';
import { useAuth } from '../context/AuthContext.jsx';
import { useAudit } from '../context/AuditContext.jsx';

export default function ProtectedRoute({ allowedRoles }) {
  const { user } = useAuth();
  const { appendAuditLog } = useAudit();
  const location = useLocation();
  const loggedDeniedRef = useRef('');

  useEffect(() => {
    if (user?.role === 'Resident' && location.pathname.startsWith('/admin') && loggedDeniedRef.current !== location.pathname) {
      loggedDeniedRef.current = location.pathname;
      appendAuditLog({ actor: user.email, action: 'FORBIDDEN_ROUTE_ACCESS', target: location.pathname, result: 'DENIED' });
    }
  }, [user, location.pathname, appendAuditLog]);

  if (!user) return <Navigate to="/login" replace state={{ from: location }} />;
  if (!allowedRoles.includes(user.role)) {
    if (user.role === 'Administrator' && location.pathname.startsWith('/resident')) {
      return <Navigate to="/admin/dashboard" replace />;
    }
    return <Navigate to="/forbidden" replace />;
  }
  return <Outlet />;
}
