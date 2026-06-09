const styles = {
  Paid: 'bg-emerald-400/15 text-emerald-100 border-emerald-300/40',
  Unpaid: 'bg-amber-400/15 text-amber-100 border-amber-300/40',
  Overdue: 'bg-rose-400/15 text-rose-100 border-rose-300/40',
  Open: 'bg-sky-400/15 text-sky-100 border-sky-300/40',
  'In Progress': 'bg-indigo-400/15 text-indigo-100 border-indigo-300/40',
  Resolved: 'bg-emerald-400/15 text-emerald-100 border-emerald-300/40',
  Rejected: 'bg-rose-400/15 text-rose-100 border-rose-300/40',
  Low: 'bg-emerald-400/15 text-emerald-100 border-emerald-300/40',
  Medium: 'bg-amber-400/15 text-amber-100 border-amber-300/40',
  High: 'bg-rose-400/15 text-rose-100 border-rose-300/40',
  SUCCESS: 'bg-emerald-400/15 text-emerald-100 border-emerald-300/40',
  DENIED: 'bg-rose-400/15 text-rose-100 border-rose-300/40',
  FAILED: 'bg-rose-400/15 text-rose-100 border-rose-300/40',
};

const labels = {
  Paid: 'Сплачено',
  Unpaid: 'Не сплачено',
  Overdue: 'Прострочено',
  Open: 'Відкрито',
  'In Progress': 'У роботі',
  Resolved: 'Вирішено',
  Rejected: 'Відхилено',
  Low: 'Низька',
  Medium: 'Середня',
  High: 'Висока',
  SUCCESS: 'Успішно',
  DENIED: 'Відмовлено',
  FAILED: 'Помилка',
  Monitoring: 'Моніторинг',
};

export default function StatusBadge({ status }) {
  return (
    <span className={`inline-flex rounded-full border px-2.5 py-1 text-xs font-semibold ${styles[status] || styles.Open}`}>
      {labels[status] || status}
    </span>
  );
}
