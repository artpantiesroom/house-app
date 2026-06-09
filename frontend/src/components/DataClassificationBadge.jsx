const styles = {
  Confidential: 'border-rose-300/50 bg-rose-400/15 text-rose-100',
  Internal: 'border-amber-300/50 bg-amber-400/15 text-amber-100',
  Public: 'border-emerald-300/50 bg-emerald-400/15 text-emerald-100',
};

const labels = {
  Confidential: 'Конфіденційно',
  Internal: 'Внутрішнє',
  Public: 'Публічне',
};

export default function DataClassificationBadge({ level }) {
  return (
    <span className={`inline-flex rounded-full border px-2.5 py-1 text-xs font-semibold ${styles[level] || styles.Internal}`}>
      {labels[level] || level}
    </span>
  );
}
