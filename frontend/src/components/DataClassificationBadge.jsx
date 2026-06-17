import { useLanguage } from '../context/LanguageContext.jsx';

const styles = {
  Confidential: 'border-rose-300/50 bg-rose-400/15 text-rose-100',
  Internal: 'border-amber-300/50 bg-amber-400/15 text-amber-100',
  Public: 'border-emerald-300/50 bg-emerald-400/15 text-emerald-100',
};

export default function DataClassificationBadge({ level }) {
  const { t } = useLanguage();
  const labelKey = {
    Confidential: 'classificationConfidential',
    Internal: 'classificationInternal',
    Public: 'classificationPublic',
  }[level];

  return (
    <span className={`inline-flex rounded-full border px-2.5 py-1 text-xs font-semibold ${styles[level] || styles.Internal}`}>
      {labelKey ? t(labelKey) : level}
    </span>
  );
}
