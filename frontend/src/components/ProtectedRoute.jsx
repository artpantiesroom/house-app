import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { useLanguage } from '../context/LanguageContext.jsx';
import LoadingSpinner from './LoadingSpinner.jsx';

export default function ProtectedRoute({ allowedRoles, allowPasswordChangeRequired = false }) {
  const { user, authReady } = useAuth();
  const { t } = useLanguage();
  const location = useLocation();

  if (!authReady) {
    return <main className="grid min-h-screen place-items-center"><LoadingSpinner label={t('restoringSession')} /></main>;
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
