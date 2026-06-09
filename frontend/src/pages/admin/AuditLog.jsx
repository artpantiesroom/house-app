import { useMemo, useState } from 'react';
import AuditLogTable from '../../components/AuditLogTable.jsx';
import { useAudit } from '../../context/AuditContext.jsx';

export default function AuditLog() {
  const { auditLog } = useAudit();
  const [filters, setFilters] = useState({ action: '', result: '', from: '', to: '' });
  const actions = [...new Set(auditLog.map((entry) => entry.action))];
  const filtered = useMemo(() => auditLog.filter((entry) => {
    const time = new Date(entry.timestamp).getTime();
    if (filters.action && entry.action !== filters.action) return false;
    if (filters.result && entry.result !== filters.result) return false;
    if (filters.from && time < new Date(filters.from).getTime()) return false;
    if (filters.to && time > new Date(`${filters.to}T23:59:59`).getTime()) return false;
    return true;
  }), [auditLog, filters]);

  return (
    <section className="space-y-5">
      <h1 className="text-3xl font-bold">Журнал аудиту</h1>
      <div className="glass grid gap-3 rounded-2xl p-4 md:grid-cols-4">
        <select value={filters.action} onChange={(e) => setFilters({ ...filters, action: e.target.value })} className="focus-ring rounded-xl border border-sky-100/15 bg-sky-950/70 px-3 py-2"><option value="">Усі дії</option>{actions.map((action) => <option key={action}>{action}</option>)}</select>
        <select value={filters.result} onChange={(e) => setFilters({ ...filters, result: e.target.value })} className="focus-ring rounded-xl border border-sky-100/15 bg-sky-950/70 px-3 py-2"><option value="">Усі результати</option><option value="SUCCESS">Успішно</option><option value="FAILED">Помилка</option><option value="DENIED">Відмовлено</option></select>
        <input aria-label="Дата від" type="date" value={filters.from} onChange={(e) => setFilters({ ...filters, from: e.target.value })} className="focus-ring rounded-xl border border-sky-100/15 bg-sky-950/70 px-3 py-2" />
        <input aria-label="Дата до" type="date" value={filters.to} onChange={(e) => setFilters({ ...filters, to: e.target.value })} className="focus-ring rounded-xl border border-sky-100/15 bg-sky-950/70 px-3 py-2" />
      </div>
      <AuditLogTable entries={filtered} />
    </section>
  );
}
