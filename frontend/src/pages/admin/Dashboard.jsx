import { useEffect, useState } from 'react';
import { Activity, AlertTriangle, CreditCard, Users } from 'lucide-react';
import SkeletonCard from '../../components/SkeletonCard.jsx';
import StatusBadge from '../../components/StatusBadge.jsx';
import { useAudit } from '../../context/AuditContext.jsx';
import { useData } from '../../context/DataContext.jsx';
import { useLanguage } from '../../context/LanguageContext.jsx';

export default function Dashboard() {
  const data = useData();
  const { auditLog } = useAudit();
  const { t } = useLanguage();
  const [ready, setReady] = useState(false);

  useEffect(() => {
    data.loadPageData(true, 700).then(setReady);
  }, []);

  const openRequests = data.requests.filter((request) => ['Open', 'In Progress'].includes(request.status)).length;
  const unpaidBills = data.payments.filter((payment) => payment.status !== 'Paid').length;

  if (!ready) return <div className="grid gap-4 md:grid-cols-2"><SkeletonCard /><SkeletonCard /><SkeletonCard /><SkeletonCard /></div>;

  return (
    <section className="space-y-5">
      <div>
        <h1 className="text-3xl font-bold">{t('adminDashboardTitle')}</h1>
        <p className="mt-2 text-sky-100/70">Цей прототип симулює базові контролі інформаційної безпеки для доступу, журналювання аудиту, керування сеансом і класифікації даних.</p>
      </div>
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {[[t('navResidents'), data.residents.length, Users], [t('navMaintenance'), openRequests, Activity], [t('navPaymentsOverview'), unpaidBills, CreditCard], [t('navSecurityIncidents'), data.incidents.length, AlertTriangle]].map(([label, value, Icon]) => (
          <div key={label} className="glass rounded-2xl p-5 transition hover:scale-[1.01]">
            <Icon className="mb-4 text-accent" />
            <p className="text-sm text-sky-100/65">{label}</p>
            <p className="text-3xl font-bold">{value}</p>
          </div>
        ))}
      </div>
      <div className="grid gap-4 xl:grid-cols-2">
        <div className="glass rounded-2xl p-5">
          <h2 className="mb-4 text-xl font-semibold">Останні інциденти безпеки</h2>
          <div className="space-y-3">{data.incidents.slice(0, 3).map((incident) => <div key={incident.id} className="rounded-xl bg-sky-400/10 p-3"><div className="flex items-center justify-between gap-3"><p className="font-semibold">{incident.title}</p><StatusBadge status={incident.severity} /></div><p className="mt-1 text-sm text-sky-100/70">{incident.recommendedAction}</p></div>)}</div>
        </div>
        <div className="glass rounded-2xl p-5">
          <h2 className="mb-4 text-xl font-semibold">Останні дії аудиту</h2>
          <div className="space-y-3">{auditLog.slice(0, 5).map((entry) => <div key={entry.id} className="rounded-xl bg-sky-400/10 p-3"><p className="font-semibold">{entry.action}</p><p className="text-sm text-sky-100/70">{entry.actor} · {entry.result}</p></div>)}</div>
        </div>
      </div>
    </section>
  );
}
