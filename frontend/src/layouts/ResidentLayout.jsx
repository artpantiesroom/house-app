import { Outlet } from 'react-router-dom';
import { motion } from 'framer-motion';
import Sidebar from '../components/Sidebar.jsx';
import SessionTimeoutModal from '../components/SessionTimeoutModal.jsx';
import FooterSecurityBadge from '../components/FooterSecurityBadge.jsx';
import { useAuth } from '../context/AuthContext.jsx';

const links = [
  { to: '/resident/home', label: 'Головна', icon: 'home' },
  { to: '/resident/requests', label: 'Заявки', icon: 'requests' },
  { to: '/resident/payments', label: 'Платежі', icon: 'payments' },
  { to: '/resident/contacts', label: 'Контакти', icon: 'contacts' },
];

export default function ResidentLayout() {
  const { user, logout, showTimeoutWarning, secondsLeft, resetActivityTimers } = useAuth();
  return (
    <div className="min-h-screen pb-28 lg:pb-0">
      <Sidebar user={user} links={links} onLogout={() => logout()} />
      <main className="px-4 py-4 lg:ml-80 lg:px-8">
        <div className="mb-4 flex items-center justify-between gap-3 lg:hidden">
          <div><p className="font-semibold">{user.email}</p><p className="text-xs text-sky-100/70">Мешканець · {new Date(user.lastLoginTime).toLocaleString()}</p></div>
          <button onClick={() => logout()} className="focus-ring rounded-xl border border-sky-100/20 px-4 py-2 text-sm">Вийти</button>
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
