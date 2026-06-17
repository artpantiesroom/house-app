import { motion } from 'framer-motion';
import { Clock } from 'lucide-react';
import { useLanguage } from '../context/LanguageContext.jsx';

export default function SessionTimeoutModal({ secondsLeft, onStaySignedIn, onLogout }) {
  const { t } = useLanguage();
  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-oceanDark/80 p-4 backdrop-blur">
      <motion.div initial={{ scale: 0.95, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} className="glass w-full max-w-sm rounded-2xl p-6">
        <div className="mb-4 flex items-center gap-3">
          <div className="rounded-xl bg-sky-400/15 p-3 text-accent">
            <Clock />
          </div>
          <div>
            <h2 className="text-lg font-semibold">{t('sessionExpiringTitle')}</h2>
            <p className="text-sm text-sky-100/70">{t('sessionExpiringBody').replace('{seconds}', secondsLeft)}</p>
          </div>
        </div>
        <div className="flex gap-3">
          <button onClick={onStaySignedIn} className="focus-ring flex-1 rounded-xl bg-primary px-4 py-3 font-semibold text-white transition hover:scale-[1.02]">
            {t('staySignedIn')}
          </button>
          <button onClick={onLogout} className="focus-ring flex-1 rounded-xl border border-sky-100/20 px-4 py-3 font-semibold text-sky-100 transition hover:scale-[1.02]">
            {t('logout')}
          </button>
        </div>
      </motion.div>
    </div>
  );
}
