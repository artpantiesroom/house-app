import { Outlet } from 'react-router-dom';
import { motion } from 'framer-motion';
import Sidebar from '../components/Sidebar.jsx';
import SessionTimeoutModal from '../components/SessionTimeoutModal.jsx';
import FooterSecurityBadge from '../components/FooterSecurityBadge.jsx';
import LanguageToggle from '../components/LanguageToggle.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { useLanguage } from '../context/LanguageContext.jsx';
import { formatDateTime } from '../utils/date.js';

export default function ResidentLayout() {
  const { user, logout, showTimeoutWarning, secondsLeft, resetActivityTimers } = useAuth();
  const { language, t } = useLanguage();
  const links = [
    { to: '/resident/home', label: t('navResidentHome'), icon: 'home' },
    { to: '/resident/requests', label: t('navMyRequests'), icon: 'requests' },
    { to: '/resident/payments', label: t('navMyPayments'), icon: 'payments' },
    { to: '/resident/contacts', label: t('navContacts'), icon: 'contacts' },
    { to: '/resident/profile', label: t('navProfile'), icon: 'profile' },
  ];
  return (
    <div className="min-h-screen pb-[calc(8rem+env(safe-area-inset-bottom))] lg:pb-0">
      <Sidebar user={user} links={links} onLogout={() => logout()} />
      <main className="px-4 py-5 lg:ml-80 lg:px-8 lg:py-6">
        <div className="mb-4 flex items-center justify-between gap-3 lg:hidden">
          <div><p className="font-semibold">{user.email}</p><p className="text-xs text-sky-100/70">{t('residentRole')} · {formatDateTime(user.lastLoginTime, language)}</p></div>
          <div className="flex items-center gap-2"><LanguageToggle compact /><button onClick={() => logout()} className="secondary-button px-4 py-2">{t('logout')}</button></div>
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
