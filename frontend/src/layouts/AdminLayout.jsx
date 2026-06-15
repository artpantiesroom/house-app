import { Outlet } from 'react-router-dom';
import { motion } from 'framer-motion';
import Sidebar from '../components/Sidebar.jsx';
import SessionTimeoutModal from '../components/SessionTimeoutModal.jsx';
import FooterSecurityBadge from '../components/FooterSecurityBadge.jsx';
import LanguageToggle from '../components/LanguageToggle.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { useLanguage } from '../context/LanguageContext.jsx';

export default function AdminLayout() {
  const { user, logout, showTimeoutWarning, secondsLeft, resetActivityTimers } = useAuth();
  const { t } = useLanguage();
  const links = [
    { to: '/admin/dashboard', label: t('navAdminDashboard'), icon: 'dashboard' },
    { to: '/admin/residents', label: t('navResidents'), icon: 'residents' },
    { to: '/admin/announcements', label: t('navAnnouncements'), icon: 'announcements' },
    { to: '/admin/maintenance', label: t('navMaintenance'), icon: 'maintenance' },
    { to: '/admin/payments', label: t('navPaymentsOverview'), icon: 'payments' },
    { to: '/admin/audit-log', label: t('navAuditLog'), icon: 'audit' },
    { to: '/admin/incidents', label: t('navSecurityIncidents'), icon: 'incidents' },
    { to: '/admin/contacts', label: t('navBuildingContacts'), icon: 'contacts' },
  ];
  return (
    <div className="min-h-screen pb-28 lg:pb-0">
      <Sidebar user={user} links={links} onLogout={() => logout()} />
      <main className="px-4 py-4 lg:ml-80 lg:px-8">
        <div className="mb-4 flex flex-wrap items-center justify-between gap-3 lg:hidden">
          <div><p className="font-semibold">{user.email}</p><p className="text-xs text-sky-100/70">{t('adminRole')} · {new Date(user.lastLoginTime).toLocaleString()}</p></div>
          <div className="flex items-center gap-2"><LanguageToggle compact /><button onClick={() => logout()} className="focus-ring rounded-xl border border-sky-100/20 px-4 py-2 text-sm">{t('logout')}</button></div>
        </div>
        <motion.div initial={{ opacity: 0, y: 14 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.28 }}>
          <Outlet />
        </motion.div>
        <div className="lg:hidden"><FooterSecurityBadge /></div>
      </main>
      {showTimeoutWarning && <SessionTimeoutModal secondsLeft={secondsLeft} onStaySignedIn={resetActivityTimers} onLogout={() => logout('INACTIVITY_TIMEOUT')} />}
    </div>
  );
}
