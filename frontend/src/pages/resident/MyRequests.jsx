import { useEffect, useState } from 'react';
import { ClipboardList } from 'lucide-react';
import LoadingSpinner from '../../components/LoadingSpinner.jsx';
import DataClassificationBadge from '../../components/DataClassificationBadge.jsx';
import EmptyState from '../../components/EmptyState.jsx';
import ErrorState from '../../components/ErrorState.jsx';
import PageHeader from '../../components/PageHeader.jsx';
import SkeletonCard from '../../components/SkeletonCard.jsx';
import StatusBadge from '../../components/StatusBadge.jsx';
import { maintenanceApi } from '../../api/maintenanceApi.js';
import { useLanguage } from '../../context/LanguageContext.jsx';
import { formatDateTime } from '../../utils/date.js';

export const requestCategories = [
  ['PLUMBING', 'plumbing'],
  ['ELECTRICITY', 'electricity'],
  ['HEATING', 'heating'],
  ['INTERNET', 'internet'],
  ['ELEVATOR', 'elevator'],
  ['CLEANING', 'cleaning'],
  ['SECURITY', 'security'],
  ['OTHER', 'other'],
];

export const requestStatuses = [
  ['NEW', 'statusNew'],
  ['IN_PROGRESS', 'statusInProgress'],
  ['WAITING_RESIDENT', 'statusWaitingResident'],
  ['RESOLVED', 'statusResolved'],
  ['CANCELLED', 'statusCancelled'],
];

export const requestPriorities = [
  ['LOW', 'priorityLow'],
  ['NORMAL', 'priorityNormal'],
  ['HIGH', 'priorityHigh'],
  ['URGENT', 'priorityUrgent'],
];

export function getRequestCategoryLabel(category, t) {
  const match = requestCategories.find(([value]) => value === category);
  return match ? t(match[1]) : category || t('other');
}

export function getRequestStatusLabel(status, t) {
  const match = requestStatuses.find(([value]) => value === status);
  return match ? t(match[1]) : status || t('statusNew');
}

export function getRequestPriorityLabel(priority, t) {
  const match = requestPriorities.find(([value]) => value === priority);
  return match ? t(match[1]) : priority || t('priorityNormal');
}

export default function MyRequests() {
  const { language, t } = useLanguage();
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState({ title: '', category: '', description: '' });
  const [errors, setErrors] = useState({});
  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      setRequests(await maintenanceApi.listResident());
    } catch {
      setError(t('requestsLoadFailed'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const validate = () => {
    const next = {};
    if (!form.title.trim()) next.title = t('titleRequired');
    if (!form.category) next.category = t('categoryRequired');
    if (!form.description.trim()) next.description = t('detailsRequired');
    if (form.description.length > 3000) next.description = t('detailsTooLong');
    setErrors(next);
    return !Object.keys(next).length;
  };

  const submit = async (event) => {
    event.preventDefault();
    if (saving || !validate()) return;
    setSaving(true);
    setSuccess('');
    setError('');
    try {
      const created = await maintenanceApi.createResident({
        title: form.title.trim(),
        description: form.description.trim(),
        category: form.category,
      });
      setRequests((current) => [created, ...current]);
      setForm({ title: '', category: '', description: '' });
      setSuccess(t('requestSent'));
    } catch (err) {
      setError(err.message || t('requestSaveFailed'));
    } finally {
      setSaving(false);
    }
  };

  const updateField = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }));
    setErrors((current) => ({ ...current, [field]: '' }));
    setSuccess('');
    setError('');
  };

  if (loading) return <SkeletonCard variant="list" count={4} />;

  return (
    <section className="space-y-5">
      <PageHeader title={t('myRequests')} subtitle={t('myRequestsSubtitle')} />
      <form onSubmit={submit} className="glass space-y-3 rounded-2xl p-4">
        <label className="block text-sm">{t('title')}<input value={form.title} maxLength={160} onChange={(event) => updateField('title', event.target.value)} className={`focus-ring mt-1 w-full rounded-xl border bg-sky-950/50 px-3 py-2 ${errors.title ? 'field-error border-rose-300' : 'border-sky-100/15'}`} />{errors.title && <span className="text-xs normal-case text-rose-200">{errors.title}</span>}</label>
        <label className="block text-sm">{t('category')}<select value={form.category} onChange={(event) => updateField('category', event.target.value)} className={`focus-ring mt-1 w-full rounded-xl border bg-sky-950/80 px-3 py-2 ${errors.category ? 'field-error border-rose-300' : 'border-sky-100/15'}`}><option value="">{t('category')}</option>{requestCategories.map(([value, labelKey]) => <option key={value} value={value}>{t(labelKey)}</option>)}</select>{errors.category && <span className="text-xs normal-case text-rose-200">{errors.category}</span>}</label>
        <label className="block text-sm">{t('details')}<textarea maxLength={3000} value={form.description} onChange={(event) => updateField('description', event.target.value)} className={`focus-ring mt-1 min-h-28 w-full rounded-xl border bg-sky-950/50 px-3 py-2 ${errors.description ? 'field-error border-rose-300' : 'border-sky-100/15'}`} />{errors.description && <span className="text-xs text-rose-200">{errors.description}</span>}</label>
        {success && <p className="rounded-xl border border-emerald-300/40 bg-emerald-400/10 p-3 text-sm text-emerald-100">{success}</p>}
        {error && <ErrorState title={t('errorTitle')} description={error} />}
        <button disabled={saving} className="primary-button">{saving ? <LoadingSpinner label={t('sending')} /> : t('submitRequest')}</button>
      </form>
      <div className="grid gap-3">
        {requests.length ? requests.map((request) => (
          <article key={request.id} className="glass rounded-2xl p-4">
            <div className="flex flex-wrap items-start justify-between gap-3">
              <div>
                <p className="font-semibold">{request.title}</p>
                <p className="mt-1 text-xs text-sky-100/55">{request.apartmentNumber || t('apartmentNotAssigned')} · {getRequestCategoryLabel(request.category, t)}</p>
              </div>
              <div className="flex flex-wrap gap-2">
                <StatusBadge status={request.status}>{getRequestStatusLabel(request.status, t)}</StatusBadge>
                <StatusBadge tone={priorityTone(request.priority)}>{getRequestPriorityLabel(request.priority, t)}</StatusBadge>
              </div>
            </div>
            <p className="mt-2 text-sm text-sky-100/75">{request.description} <DataClassificationBadge level="Internal" /></p>
            {request.adminResponse && <p className="mt-3 rounded-xl border border-emerald-300/30 bg-emerald-400/10 p-3 text-sm text-emerald-50"><span className="font-semibold">{t('adminResponse')}:</span> {request.adminResponse}</p>}
            <p className="mt-3 text-xs text-sky-100/55">{t('createdAt')}: {formatDateTime(request.createdAt, language)} · {t('updatedAt')}: {formatDateTime(request.updatedAt, language)}</p>
          </article>
        )) : <EmptyState icon={ClipboardList} title={t('noRequests')} description={t('noResidentRequestsDescription')} />}
      </div>
    </section>
  );
}

function priorityTone(priority) {
  if (priority === 'URGENT' || priority === 'HIGH') return 'danger';
  if (priority === 'NORMAL') return 'warning';
  return 'neutral';
}
