const toneStyles = {
  neutral: 'border-sky-300/30 bg-sky-400/10 text-sky-100',
  success: 'border-emerald-300/45 bg-emerald-400/15 text-emerald-100',
  warning: 'border-amber-300/45 bg-amber-400/15 text-amber-100',
  danger: 'border-rose-300/45 bg-rose-400/15 text-rose-100',
};

const statusTones = {
  ACTIVE: 'success',
  PAID: 'success',
  COMPLETED: 'success',
  RESOLVED: 'success',
  CLOSED: 'success',
  PUBLISHED: 'success',
  ENABLED: 'success',
  OPEN: 'warning',
  NEW: 'warning',
  PENDING: 'warning',
  IN_PROGRESS: 'warning',
  INVESTIGATING: 'warning',
  WAITING_RESIDENT: 'warning',
  OVERDUE: 'danger',
  CANCELLED: 'danger',
  CANCELED: 'danger',
  DISABLED: 'danger',
  FALSE_POSITIVE: 'neutral',
  DRAFT: 'neutral',
  ARCHIVED: 'neutral',
};

export default function StatusBadge({ children, status, tone = 'neutral' }) {
  const resolvedTone = statusTones[String(status || '').toUpperCase()] || tone;
  return (
    <span className={`inline-flex items-center rounded-full border px-2.5 py-1 text-xs font-semibold leading-none ${toneStyles[resolvedTone] || toneStyles.neutral}`}>
      {children}
    </span>
  );
}
