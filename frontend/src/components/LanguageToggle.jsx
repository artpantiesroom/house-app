import { useLanguage } from '../context/LanguageContext.jsx';

export default function LanguageToggle({ compact = false }) {
  const { language, setLanguage, t } = useLanguage();

  return (
    <div className={`flex items-center gap-1 rounded-xl border border-sky-100/15 bg-sky-950/40 p-1 shadow-sm ${compact ? '' : 'w-fit'}`} aria-label={t('language')}>
      {['uk', 'en'].map((option) => (
        <button
          key={option}
          type="button"
          onClick={() => setLanguage(option)}
          aria-label={`${t('switchLanguage')}: ${option === 'uk' ? 'UA' : 'EN'}`}
          className={`focus-ring min-h-9 rounded-lg px-2.5 py-1 text-xs font-semibold transition ${language === option ? 'bg-primary text-white shadow-glass' : 'text-sky-100/70 hover:bg-sky-400/10 hover:text-sky-50'}`}
        >
          {option === 'uk' ? 'UA' : 'EN'}
        </button>
      ))}
    </div>
  );
}
