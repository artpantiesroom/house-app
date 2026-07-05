import { useEffect, useState } from 'react';
import { ClipboardList } from 'lucide-react';
import DataClassificationBadge from '../../components/DataClassificationBadge.jsx';
import EmptyState from '../../components/EmptyState.jsx';
import ErrorState from '../../components/ErrorState.jsx';
import LoadingSpinner from '../../components/LoadingSpinner.jsx';
import SkeletonCard from '../../components/SkeletonCard.jsx';
import StatusBadge from '../../components/StatusBadge.jsx';
import { maintenanceApi } from '../../api/maintenanceApi.js';
import { useLanguage } from '../../context/LanguageContext.jsx';
import { formatDateTime } from '../../utils/date.js';
import {
  getRequestCategoryLabel,
  getRequestPriorityLabel,
  getRequestStatusLabel,
  requestCategories,
  requestPriorities,
  requestStatuses,
} from '../resident/MyRequests.jsx';

const emptyFilters = { status: '', category: '', priority: '', search: '' };

export default function MaintenanceAdmin() {
  const { language, t } = useLanguage();
  const [requests, setRequests] = useState([]);
  const [filters, setFilters] = useState(emptyFilters);
  const [selected, setSelected] = useState(null);
  const [form, setForm] = useState({ status: 'NEW', priority: 'NORMAL', adminResponse: '', internalNotes: '' });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const load = async (nextFilters = filters) => {
    setLoading(true);
    setError('');
    try {
      const items = await maintenanceApi.listAdmin(nextFilters);
      setRequests(items);
      if (selected) {
        const refreshed = items.find((item) => item.id === selected.id);
        if (refreshed) select(refreshed);
      }
    } catch {
      setError(t('requestsLoadFailed'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const select = (request) => {
    setSelected(request);
    setForm({
      status: request.status,
      priority: request.priority,
      adminResponse: request.adminResponse || '',
      internalNotes: request.internalNotes || '',
    });
    setSuccess('');
    setError('');
  };

  const update = async (event) => {
    event.preventDefault();
    if (!selected || saving) return;
    setSaving(true);
    setError('');
    setSuccess('');
    try {
      const updated = await maintenanceApi.updateAdmin(selected.id, {
        status: form.status,
        priority: form.priority,
        adminResponse: form.adminResponse.trim() || null,
        internalNotes: form.internalNotes.trim() || null,
      });
      setSelected(updated);
      setRequests((current) => current.map((item) => (item.id === updated.id ? updated : item)));
      setSuccess(t('requestUpdated'));
    } catch (err) {
      setError(err.message || t('requestSaveFailed'));
    } finally {
      setSaving(false);
    }
  };

  const applyFilters = () => {
    load(filters);
  };

  return (
    <section className="space-y-5">
      <h1 className="text-3xl font-bold">{t('maintenanceTitle')}</h1>

      <div className="glass grid gap-3 rounded-2xl p-4 md:grid-cols-5">
        <FilterSelect label={t('status')} value={filters.status} onChange={(value) => setFilters((current) => ({ ...current, status: value }))} options={requestStatuses} allLabel={t('all')} t={t} />
        <FilterSelect label={t('category')} value={filters.category} onChange={(value) => setFilters((current) => ({ ...current, category: value }))} options={requestCategories} allLabel={t('all')} t={t} />
        <FilterSelect label={t('priority')} value={filters.priority} onChange={(value) => setFilters((current) => ({ ...current, priority: value }))} options={requestPriorities} allLabel={t('all')} t={t} />
        <label className="block text-sm">{t('search')}<input value={filters.search} onChange={(event) => setFilters((current) => ({ ...current, search: event.target.value }))} className="field-control" /></label>
        <div className="flex items-end gap-2"><button onClick={applyFilters} className="primary-button w-full text-sm">{t('applyFilters')}</button></div>
      </div>

      {error && <ErrorState title={t('errorTitle')} description={error} onRetry={() => load(filters)} retryLabel={t('retry')} />}
      {success && <p className="rounded-xl border border-emerald-300/40 bg-emerald-400/10 p-3 text-sm text-emerald-100">{success}</p>}

      {loading ? <SkeletonCard variant="list" count={5} /> : (
        <div className="grid gap-4 xl:grid-cols-[1.2fr_0.8fr]">
          <div className="grid gap-3">
            {requests.length ? requests.map((request) => (
              <article key={request.id} className={`glass cursor-pointer rounded-2xl p-4 transition ${selected?.id === request.id ? 'ring-2 ring-accent' : ''}`} onClick={() => select(request)}>
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div>
                    <h2 className="text-lg font-semibold">{request.title}</h2>
                    <p className="text-sm text-sky-100/70">{request.residentName} · {request.residentEmail}</p>
                    <p className="text-xs text-sky-100/55">{request.apartmentNumber || t('apartmentNotAssigned')} · {t('sectionLabel')} {request.buildingSection || '—'} · {t('floorLabel')} {request.floor ?? '—'}</p>
                  </div>
                  <DataClassificationBadge level="Internal" />
                </div>
                <p className="mt-2 text-sm text-sky-100/75">{request.description}</p>
                <div className="mt-3 flex flex-wrap gap-2 text-xs">
                  <StatusBadge status={request.status}>{getRequestStatusLabel(request.status, t)}</StatusBadge>
                  <StatusBadge tone={priorityTone(request.priority)}>{getRequestPriorityLabel(request.priority, t)}</StatusBadge>
                  <StatusBadge>{getRequestCategoryLabel(request.category, t)}</StatusBadge>
                </div>
                <p className="mt-3 text-xs text-sky-100/55">{t('createdAt')}: {formatDateTime(request.createdAt, language)}</p>
              </article>
            )) : <EmptyState icon={ClipboardList} title={t('noRequests')} description={t('noRequestsDescription')} />}
          </div>

          <form onSubmit={update} className="glass h-fit space-y-3 rounded-2xl p-4">
            {selected ? (
              <>
                <div>
                  <h2 className="text-xl font-semibold">{selected.title}</h2>
                  <p className="text-sm text-sky-100/65">{t('resident')}: {selected.residentName} · {selected.apartmentNumber || t('apartmentNotAssigned')}</p>
                </div>
                <FilterSelect label={t('status')} value={form.status} onChange={(value) => setForm((current) => ({ ...current, status: value }))} options={requestStatuses} t={t} />
                <FilterSelect label={t('priority')} value={form.priority} onChange={(value) => setForm((current) => ({ ...current, priority: value }))} options={requestPriorities} t={t} />
                <label className="block text-sm">{t('adminResponse')}<textarea maxLength={3000} value={form.adminResponse} onChange={(event) => setForm((current) => ({ ...current, adminResponse: event.target.value }))} className="field-control min-h-28" /></label>
                <label className="block text-sm">{t('internalNotes')}<textarea maxLength={3000} value={form.internalNotes} onChange={(event) => setForm((current) => ({ ...current, internalNotes: event.target.value }))} className="field-control min-h-28" /></label>
                <p className="text-xs text-sky-100/55">{t('updatedAt')}: {formatDateTime(selected.updatedAt, language)} · {t('resolvedAt')}: {formatDateTime(selected.resolvedAt, language)}</p>
                <button disabled={saving} className="primary-button">{saving ? <LoadingSpinner label={t('saving')} /> : t('updateRequest')}</button>
              </>
            ) : <p className="text-sm text-sky-100/70">{t('selectRequest')}</p>}
          </form>
        </div>
      )}
    </section>
  );
}

function FilterSelect({ label, value, onChange, options, allLabel, t }) {
  return (
    <label className="block text-sm">{label}
      <select value={value} onChange={(event) => onChange(event.target.value)} className="field-control">
        {allLabel && <option value="">{allLabel}</option>}
        {options.map(([optionValue, labelKey]) => <option key={optionValue} value={optionValue}>{t(labelKey)}</option>)}
      </select>
    </label>
  );
}

function priorityTone(priority) {
  if (priority === 'URGENT' || priority === 'HIGH') return 'danger';
  if (priority === 'NORMAL') return 'warning';
  return 'neutral';
}
