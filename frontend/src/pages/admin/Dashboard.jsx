import { useEffect, useMemo, useState } from 'react';
import { Activity, AlertTriangle, CreditCard, Users } from 'lucide-react';
import { auditApi } from '../../api/auditApi.js';
import { incidentsApi } from '../../api/incidentsApi.js';
import { maintenanceApi } from '../../api/maintenanceApi.js';
import { paymentsApi } from '../../api/paymentsApi.js';
import { residentsApi } from '../../api/residentsApi.js';
import SkeletonCard from '../../components/SkeletonCard.jsx';
import { useLanguage } from '../../context/LanguageContext.jsx';
import { getPaymentStatusLabel } from '../resident/MyPayments.jsx';
import { getRequestStatusLabel } from '../resident/MyRequests.jsx';

export default function Dashboard() {
  const { t } = useLanguage();
  const [summary, setSummary] = useState({
    residents: [],
    requests: [],
    payments: [],
    incidents: [],
    auditRecords: [],
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const [residents, requests, payments, incidents, auditRecords] = await Promise.all([
        residentsApi.list(),
        maintenanceApi.listAdmin({}),
        paymentsApi.listAdmin({}),
        incidentsApi.list({}),
        auditApi.list({}),
      ]);
      setSummary({ residents, requests, payments, incidents, auditRecords });
    } catch (loadError) {
      setError(loadError.message || t('dashboardLoadFailed'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const counts = useMemo(() => {
    const activeRequestStatuses = new Set(['NEW', 'IN_PROGRESS', 'WAITING_RESIDENT']);
    const activeIncidentStatuses = new Set(['OPEN', 'INVESTIGATING']);
    return {
      residents: summary.residents.length,
      activeRequests: summary.requests.filter((request) => activeRequestStatuses.has(request.status)).length,
      unpaidPayments: summary.payments.filter((payment) => !['PAID', 'CANCELLED'].includes(payment.status)).length,
      activeIncidents: summary.incidents.filter((incident) => activeIncidentStatuses.has(incident.status)).length,
    };
  }, [summary]);

  if (loading) {
    return <div className="grid gap-4 md:grid-cols-2"><SkeletonCard /><SkeletonCard /><SkeletonCard /><SkeletonCard /></div>;
  }

  return (
    <section className="space-y-5">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h1 className="text-3xl font-bold">{t('adminDashboardTitle')}</h1>
          <p className="mt-2 max-w-3xl text-sky-100/70">{t('dashboardSubtitle')}</p>
        </div>
        <button onClick={load} className="focus-ring rounded-xl border border-sky-100/20 px-4 py-2 text-sm">{t('refresh')}</button>
      </div>

      {error && <p className="rounded-xl border border-rose-300/40 bg-rose-950/40 p-3 text-sm text-rose-100">{error}</p>}

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {[
          [t('navResidents'), counts.residents, Users],
          [t('navMaintenance'), counts.activeRequests, Activity],
          [t('navPaymentsOverview'), counts.unpaidPayments, CreditCard],
          [t('navSecurityIncidents'), counts.activeIncidents, AlertTriangle],
        ].map(([label, value, Icon]) => (
          <div key={label} className="glass rounded-2xl p-5 transition hover:scale-[1.01]">
            <Icon className="mb-4 text-accent" />
            <p className="text-sm text-sky-100/65">{label}</p>
            <p className="text-3xl font-bold">{value}</p>
          </div>
        ))}
      </div>

      <div className="grid gap-4 xl:grid-cols-2">
        <div className="glass rounded-2xl p-5">
          <h2 className="mb-4 text-xl font-semibold">{t('latestIncidents')}</h2>
          <div className="space-y-3">
            {!summary.incidents.length && <p className="rounded-xl bg-sky-400/10 p-3 text-sm text-sky-100/70">{t('noIncidents')}</p>}
            {summary.incidents.slice(0, 3).map((incident) => (
              <div key={incident.id} className="rounded-xl bg-sky-400/10 p-3">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <p className="font-semibold">{incident.title}</p>
                  <Badge>{t(`incidentSeverity${incident.severity}`)}</Badge>
                </div>
                <p className="mt-1 text-sm text-sky-100/70">{t(`incidentStatus${incident.status}`)} · {t(`incidentCategory${incident.category}`)}</p>
              </div>
            ))}
          </div>
        </div>
        <div className="glass rounded-2xl p-5">
          <h2 className="mb-4 text-xl font-semibold">{t('latestAudit')}</h2>
          <div className="space-y-3">
            {!summary.auditRecords.length && <p className="rounded-xl bg-sky-400/10 p-3 text-sm text-sky-100/70">{t('auditEmpty')}</p>}
            {summary.auditRecords.slice(0, 5).map((entry) => (
              <div key={entry.id} className="rounded-xl bg-sky-400/10 p-3">
                <p className="font-semibold">{t(`auditAction${entry.action}`)}</p>
                <p className="text-sm text-sky-100/70">{entry.actorEmail || t('systemActor')} · {entry.result || 'SUCCESS'}</p>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="grid gap-4 xl:grid-cols-2">
        <CompactList
          title={t('maintenanceTitle')}
          emptyLabel={t('noRequests')}
          items={summary.requests.slice(0, 4)}
          render={(request) => (
            <>
              <p className="font-semibold">{request.title}</p>
              <p className="text-sm text-sky-100/70">{request.residentName || t('unknownResident')} · {getRequestStatusLabel(request.status, t)}</p>
            </>
          )}
        />
        <CompactList
          title={t('paymentsOverviewTitle')}
          emptyLabel={t('noPayments')}
          items={summary.payments.slice(0, 4)}
          render={(payment) => (
            <>
              <p className="font-semibold">{payment.residentName || t('unknownResident')}</p>
              <p className="text-sm text-sky-100/70">{payment.periodMonth}/{payment.periodYear} · {getPaymentStatusLabel(payment.status, t)}</p>
            </>
          )}
        />
      </div>
    </section>
  );
}

function CompactList({ title, emptyLabel, items, render }) {
  return (
    <div className="glass rounded-2xl p-5">
      <h2 className="mb-4 text-xl font-semibold">{title}</h2>
      <div className="space-y-3">
        {!items.length && <p className="rounded-xl bg-sky-400/10 p-3 text-sm text-sky-100/70">{emptyLabel}</p>}
        {items.map((item) => <div key={item.id} className="rounded-xl bg-sky-400/10 p-3">{render(item)}</div>)}
      </div>
    </div>
  );
}

function Badge({ children }) {
  return <span className="rounded-full border border-sky-100/15 bg-sky-950/50 px-2 py-1 text-xs">{children}</span>;
}
