import { NavLink } from 'react-router-dom';
import { Bell, Building2, ClipboardList, CreditCard, FileClock, Home, LayoutDashboard, LogOut, ShieldAlert, UserCircle, Users } from 'lucide-react';
import FooterSecurityBadge from './FooterSecurityBadge.jsx';
import LanguageToggle from './LanguageToggle.jsx';
import { useLanguage } from '../context/LanguageContext.jsx';
import { formatDateTime } from '../utils/date.js';

const iconMap = { dashboard: LayoutDashboard, residents: Users, announcements: Bell, maintenance: ClipboardList, payments: CreditCard, audit: FileClock, incidents: ShieldAlert, contacts: Building2, home: Home, requests: ClipboardList, profile: UserCircle };

export default function Sidebar({ user, links, onLogout }) {
  const { language, t } = useLanguage();
  return (
    <>
      <aside className="glass fixed left-4 top-4 hidden h-[calc(100vh-2rem)] w-72 flex-col rounded-2xl p-4 lg:flex">
        <div className="mb-6 flex items-center gap-3">
          <div className="grid h-11 w-11 place-items-center rounded-xl bg-primary text-white"><Building2 /></div>
          <div>
            <p className="font-semibold">{t('appName')}</p>
            <p className="text-xs text-sky-100/65">{user.email}</p>
          </div>
        </div>
        <div className="mb-5 rounded-xl bg-sky-400/10 p-3 text-xs text-sky-100/75">
          <p className="font-semibold text-sky-50">{user.role === 'ADMIN' ? t('adminRole') : t('residentRole')}</p>
          <p>{t('lastLogin')}: {formatDateTime(user.lastLoginTime, language)}</p>
        </div>
        <div className="mb-4"><LanguageToggle /></div>
        <nav className="flex flex-1 flex-col gap-2">
          {links.map((link) => {
            const Icon = iconMap[link.icon] || Home;
            return (
              <NavLink key={link.to} to={link.to} className={({ isActive }) => `focus-ring flex items-center gap-3 rounded-xl px-3 py-3 text-sm transition hover:scale-[1.02] ${isActive ? 'bg-primary text-white' : 'text-sky-100/75 hover:bg-sky-400/10'}`}>
                <Icon size={18} /> {link.label}
              </NavLink>
            );
          })}
        </nav>
        <button onClick={onLogout} className="focus-ring mt-4 flex items-center justify-center gap-2 rounded-xl border border-sky-100/15 px-3 py-3 text-sm font-semibold text-sky-100 transition hover:scale-[1.02]">
          <LogOut size={18} /> {t('logout')}
        </button>
        <FooterSecurityBadge />
      </aside>
      <nav className="glass fixed inset-x-3 bottom-3 z-30 flex gap-1 overflow-x-auto rounded-2xl p-2 lg:hidden" aria-label="Mobile primary navigation">
        {links.map((link) => {
          const Icon = iconMap[link.icon] || Home;
          return (
            <NavLink key={link.to} to={link.to} className={({ isActive }) => `focus-ring flex min-w-24 flex-col items-center rounded-xl px-2 py-2 text-center text-[11px] leading-tight ${isActive ? 'bg-primary text-white' : 'text-sky-100/70'}`}>
              <Icon size={18} /> {link.label}
            </NavLink>
          );
        })}
      </nav>
    </>
  );
}
