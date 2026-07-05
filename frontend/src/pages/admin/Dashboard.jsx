import { useEffect, useMemo, useState } from 'react';
import { Activity, AlertTriangle, CreditCard, FileClock, Users } from 'lucide-react';
import { auditApi } from '../../api/auditApi.js';
import { incidentsApi } from '../../api/incidentsApi.js';
import { maintenanceApi } from '../../api/maintenanceApi.js';
import { paymentsApi } from '../../api/paymentsApi.js';
import { residentsApi } from '../../api/residentsApi.js';
import SkeletonCard from '../../components/SkeletonCard.jsx';
import EmptyState from '../../components/EmptyState.jsx';
import ErrorState from '../../components/ErrorState.jsx';
import PageHeader from '../../components/PageHeader.jsx';
import StatusBadge from '../../components/StatusBadge.jsx';
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
    return <div className="space-y-4"><SkeletonCard variant="kpi" count={4} /><SkeletonCard variant="list" count={3} /></div>;
  }

  return (
    <section className="space-y-5">
      <PageHeader title={t('adminDashboardTitle')} subtitle={t('dashboardSubtitle')} action={<button onClick={load} className="secondary-button">{t('refresh')}</button>} />

      {error && <ErrorState title={t('errorTitle')} description={error} onRetry={load} retryLabel={t('retry')} />}

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {[
          [t('navResidents'), counts.residents, Users],
          [t('navMaintenance'), counts.activeRequests, Activity],
          [t('navPaymentsOverview'), counts.unpaidPayments, CreditCard],
          [t('navSecurityIncidents'), counts.activeIncidents, AlertTriangle],
        ].map(([label, value, Icon]) => (
          <div key={label} className="glass flex min-h-44 flex-col justify-between rounded-3xl p-6">
            <div className="mb-5 grid h-12 w-12 place-items-center rounded-2xl border border-sky-100/15 bg-sky-400/10 text-accent shadow-sm">
              <Icon size={24} aria-hidden="true" />
            </div>
            <p className="text-sm text-sky-100/65">{label}</p>
            <p className="mt-2 text-4xl font-bold leading-none">{value}</p>
          </div>
        ))}
      </div>

      <div className="grid gap-4 xl:grid-cols-2">
        <div className="glass rounded-3xl p-5">
          <h2 className="mb-4 text-xl font-semibold">{t('latestIncidents')}</h2>
          <div className="space-y-3">
            {!summary.incidents.length && <EmptyState icon={AlertTriangle} title={t('noIncidents')} description={t('noIncidentsDescription')} />}
            {summary.incidents.slice(0, 3).map((incident) => (
              <div key={incident.id} className="rounded-2xl border border-sky-100/10 bg-sky-400/10 p-3 transition hover:border-primary/40 hover:bg-sky-400/15">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <p className="font-semibold">{incident.title}</p>
                  <StatusBadge tone={severityTone(incident.severity)}>{t(`incidentSeverity${incident.severity}`)}</StatusBadge>
                </div>
                <div className="mt-2 flex flex-wrap gap-2">
                  <StatusBadge status={incident.status}>{t(`incidentStatus${incident.status}`)}</StatusBadge>
                  <StatusBadge>{t(`incidentCategory${incident.category}`)}</StatusBadge>
                </div>
              </div>
            ))}
          </div>
        </div>
        <div className="glass rounded-3xl p-5">
          <h2 className="mb-4 text-xl font-semibold">{t('latestAudit')}</h2>
          <div className="space-y-3">
            {!summary.auditRecords.length && <EmptyState icon={FileClock} title={t('auditEmpty')} description={t('auditEmptyDescription')} />}
            {summary.auditRecords.slice(0, 5).map((entry) => (
              <div key={entry.id} className="rounded-2xl border border-sky-100/10 bg-sky-400/10 p-3 transition hover:border-primary/40 hover:bg-sky-400/15">
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
              <p className="text-sm text-sky-100/70">{request.residentName || t('unknownResident')}</p>
              <div className="mt-2"><StatusBadge status={request.status}>{getRequestStatusLabel(request.status, t)}</StatusBadge></div>
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
              <p className="text-sm text-sky-100/70">{payment.periodMonth}/{payment.periodYear}</p>
              <div className="mt-2"><StatusBadge status={payment.status}>{getPaymentStatusLabel(payment.status, t)}</StatusBadge></div>
            </>
          )}
        />
      </div>
    </section>
  );
}

function CompactList({ title, emptyLabel, items, render }) {
  return (
    <div className="glass rounded-3xl p-5">
      <h2 className="mb-4 text-xl font-semibold">{title}</h2>
      <div className="space-y-3">
        {!items.length && <EmptyState title={emptyLabel} description="" />}
        {items.map((item) => <div key={item.id} className="rounded-2xl border border-sky-100/10 bg-sky-400/10 p-3 transition hover:border-primary/50 hover:bg-sky-400/15">{render(item)}</div>)}
      </div>
    </div>
  );
}


function severityTone(severity) {
  if (severity === 'CRITICAL' || severity === 'HIGH') return 'danger';
  if (severity === 'MEDIUM') return 'warning';
  return 'success';
}
