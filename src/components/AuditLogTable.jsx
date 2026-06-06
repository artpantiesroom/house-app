import StatusBadge from './StatusBadge.jsx';

const actionLabels = {
  SESSION_RESTORE: 'Відновлення сеансу',
  ANNOUNCEMENT_CREATED: 'Оголошення створено',
  ANNOUNCEMENT_EDITED: 'Оголошення змінено',
  ANNOUNCEMENT_DELETED: 'Оголошення видалено',
  MAINTENANCE_STATUS_CHANGED: 'Статус заявки змінено',
  LOGIN: 'Вхід',
  LOGOUT: 'Вихід',
  REQUEST_CREATED: 'Заявку створено',
  RESIDENT_CREATED: 'Мешканця створено',
  RESIDENT_EDITED: 'Мешканця змінено',
  RESIDENT_DELETED: 'Мешканця видалено',
  FORBIDDEN_ROUTE_ACCESS: 'Заборонений доступ',
};

export default function AuditLogTable({ entries }) {
  if (!entries.length) {
    return <div className="glass rounded-2xl p-5 text-sm text-sky-100/75">Немає записів аудиту за поточними фільтрами.</div>;
  }

  return (
    <div className="overflow-x-auto rounded-2xl border border-sky-100/10">
      <table className="min-w-full divide-y divide-sky-100/10 text-left text-sm">
        <thead className="bg-sky-950/40 text-xs uppercase text-sky-100/70">
          <tr>
            <th className="px-4 py-3">Час</th>
            <th className="px-4 py-3">Користувач</th>
            <th className="px-4 py-3">Дія</th>
            <th className="px-4 py-3">Об'єкт</th>
            <th className="px-4 py-3">Результат</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-sky-100/10">
          {entries.map((entry) => (
            <tr key={entry.id} className="bg-oceanDark/35">
              <td className="whitespace-nowrap px-4 py-3">{new Date(entry.timestamp).toLocaleString()}</td>
              <td className="px-4 py-3 text-sky-100/80">{entry.actor}</td>
              <td className="px-4 py-3 font-semibold">{actionLabels[entry.action] || entry.action}</td>
              <td className="px-4 py-3 text-sky-100/80">{entry.target}</td>
              <td className="px-4 py-3"><StatusBadge status={entry.result} /></td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
