import { Lock } from 'lucide-react';
import { useLanguage } from '../context/LanguageContext.jsx';

export default function FooterSecurityBadge() {
  const { t } = useLanguage();
  return (
    <div className="mt-5 flex items-center justify-center gap-2 text-xs text-sky-100/55">
      <Lock size={14} />
      <span>{t('prototypeConnectionNotice')}</span>
    </div>
  );
}
