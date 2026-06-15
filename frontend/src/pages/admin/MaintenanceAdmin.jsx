import { useEffect, useState } from 'react';
import DataClassificationBadge from '../../components/DataClassificationBadge.jsx';
import SkeletonCard from '../../components/SkeletonCard.jsx';
import StatusBadge from '../../components/StatusBadge.jsx';
import { useAudit } from '../../context/AuditContext.jsx';
import { useAuth } from '../../context/AuthContext.jsx';
import { useData } from '../../context/DataContext.jsx';
import { useLanguage } from '../../context/LanguageContext.jsx';
import { getRequestCategoryLabel } from '../resident/MyRequests.jsx';

const statuses = ['Open', 'In Progress', 'Resolved', 'Rejected'];
const statusLabels = {
  Open: 'statusOpen',
  'In Progress': 'statusInProgress',
  Resolved: 'statusResolved',
  Rejected: 'statusRejected',
};

export default function MaintenanceAdmin() {
  const data = useData();
  const { user } = useAuth();
  const { t } = useLanguage();
  const { appendAuditLog } = useAudit();
  const [ready, setReady] = useState(false);
  const [savingId, setSavingId] = useState('');
  useEffect(() => { data.loadPageData(true, 750).then(setReady); }, []);

  const changeStatus = async (request, status) => {
    setSavingId(request.id);
    await data.changeRequestStatus(request.id, status);
    appendAuditLog({ actor: user.email, action: 'MAINTENANCE_STATUS_CHANGED', target: request.id, result: 'SUCCESS' });
    setSavingId('');
  };

  if (!ready) return <SkeletonCard rows={6} />;
  return (
    <section className="space-y-5">
      <h1 className="text-3xl font-bold">{t('maintenanceTitle')}</h1>
      <div className="grid gap-3">
        {data.requests.map((request) => {
          const resident = data.residents.find((item) => item.id === request.residentId);
          return (
            <article key={request.id} className="glass rounded-2xl p-4">
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div><h2 className="text-lg font-semibold">{request.title}</h2><p className="text-sm text-sky-100/70">{resident?.name || t('unknownResident')} · {getRequestCategoryLabel(request.category, t)} <DataClassificationBadge level="Internal" /></p><p className="mt-2 text-sm text-sky-100/75">{request.details}</p></div>
                <StatusBadge status={request.status} />
              </div>
              <div className="mt-4 flex flex-wrap gap-2">
                {statuses.map((status) => <button key={status} disabled={savingId === request.id || status === request.status} onClick={() => changeStatus(request, status)} className="focus-ring rounded-xl border border-sky-100/20 px-3 py-2 text-sm disabled:opacity-50">{savingId === request.id && status !== request.status ? t('saving') : t(statusLabels[status])}</button>)}
              </div>
            </article>
          );
        })}
      </div>
    </section>
  );
}
