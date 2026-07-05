import { useEffect, useState } from 'react';
import { ShieldAlert } from 'lucide-react';
import { incidentsApi } from '../../api/incidentsApi.js';
import EmptyState from '../../components/EmptyState.jsx';
import ErrorState from '../../components/ErrorState.jsx';
import LoadingSpinner from '../../components/LoadingSpinner.jsx';
import PageHeader from '../../components/PageHeader.jsx';
import SkeletonCard from '../../components/SkeletonCard.jsx';
import StatusBadge from '../../components/StatusBadge.jsx';
import { useLanguage } from '../../context/LanguageContext.jsx';
import { formatDateTime } from '../../utils/date.js';

const severities = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
const statuses = ['OPEN', 'INVESTIGATING', 'RESOLVED', 'FALSE_POSITIVE'];
const categories = ['AUTHENTICATION', 'AUTHORIZATION', 'DATA_ACCESS', 'PAYMENT', 'MAINTENANCE', 'SYSTEM', 'OTHER'];

const emptyForm = {
  title: '',
  description: '',
  severity: 'MEDIUM',
  status: 'OPEN',
  category: 'SYSTEM',
  assignedToUserId: '',
  relatedAuditLogId: '',
  resolutionNotes: '',
};

export default function Incidents() {
  const { language, t } = useLanguage();
  const [records, setRecords] = useState([]);
  const [filters, setFilters] = useState({ severity: '', status: '', category: '', search: '' });
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [busyId, setBusyId] = useState(null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const load = async (nextFilters = filters) => {
    setLoading(true);
    setError('');
    try {
      setRecords(await incidentsApi.list(nextFilters));
    } catch (err) {
      setError(err.message || t('incidentsLoadFailed'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const submit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    setSuccess('');
    try {
      const payload = toPayload(form);
      if (editingId) {
        await incidentsApi.update(editingId, payload);
      } else {
        await incidentsApi.create(payload);
      }
      setForm(emptyForm);
      setEditingId(null);
      setSuccess(t('incidentSaved'));
      await load();
    } catch (err) {
      setError(err.message || t('incidentSaveFailed'));
    } finally {
      setSaving(false);
    }
  };

  const edit = (incident) => {
    setEditingId(incident.id);
    setForm({
      title: incident.title || '',
      description: incident.description || '',
      severity: incident.severity || 'MEDIUM',
      status: incident.status || 'OPEN',
      category: incident.category || 'SYSTEM',
      assignedToUserId: incident.assignedToUserId || '',
      relatedAuditLogId: incident.relatedAuditLogId || '',
      resolutionNotes: incident.resolutionNotes || '',
    });
    setError('');
    setSuccess('');
  };

  const updateStatus = async (incident, status) => {
    setBusyId(incident.id);
    setError('');
    try {
      await incidentsApi.updateStatus(incident.id, {
        status,
        resolutionNotes: status === 'RESOLVED' ? (incident.resolutionNotes || t('incidentResolvedNote')) : incident.resolutionNotes,
        assignedToUserId: incident.assignedToUserId,
      });
      await load();
    } catch (err) {
      setError(err.message || t('incidentSaveFailed'));
    } finally {
      setBusyId(null);
    }
  };

  const markFalsePositive = async (incident) => {
    setBusyId(incident.id);
    setError('');
    try {
      await incidentsApi.softDelete(incident.id);
      await load();
    } catch (err) {
      setError(err.message || t('incidentSaveFailed'));
    } finally {
      setBusyId(null);
    }
  };

  return (
    <section className="space-y-5">
      <PageHeader title={t('securityIncidentsTitle')} subtitle={t('securityIncidentsSubtitle')} />

      <form onSubmit={submit} className="glass space-y-4 rounded-2xl p-4">
        <div className="grid gap-3 md:grid-cols-3">
          <TextInput label={t('title')} value={form.title} onChange={(value) => setForm((current) => ({ ...current, title: value }))} required maxLength={160} />
          <Select label={t('severity')} value={form.severity} onChange={(value) => setForm((current) => ({ ...current, severity: value }))} options={severities} labelFor={(value) => t(`incidentSeverity${value}`)} />
          <Select label={t('status')} value={form.status} onChange={(value) => setForm((current) => ({ ...current, status: value }))} options={statuses} labelFor={(value) => t(`incidentStatus${value}`)} />
          <Select label={t('category')} value={form.category} onChange={(value) => setForm((current) => ({ ...current, category: value }))} options={categories} labelFor={(value) => t(`incidentCategory${value}`)} />
          <TextInput label={t('assignedToUserId')} type="number" value={form.assignedToUserId} onChange={(value) => setForm((current) => ({ ...current, assignedToUserId: value }))} />
          <TextInput label={t('relatedAuditLogId')} type="number" value={form.relatedAuditLogId} onChange={(value) => setForm((current) => ({ ...current, relatedAuditLogId: value }))} />
          <TextArea label={t('details')} value={form.description} onChange={(value) => setForm((current) => ({ ...current, description: value }))} required />
          <TextArea label={t('resolutionNotes')} value={form.resolutionNotes} onChange={(value) => setForm((current) => ({ ...current, resolutionNotes: value }))} />
        </div>
        {error && <ErrorState title={t('errorTitle')} description={error} />}
        {success && <p className="rounded-xl border border-emerald-300/40 bg-emerald-400/10 p-3 text-sm text-emerald-100">{success}</p>}
        <div className="flex flex-wrap gap-2">
          <button disabled={saving} className="primary-button">{saving ? <LoadingSpinner label={t('saving')} /> : editingId ? t('updateIncident') : t('createIncident')}</button>
          {editingId && <button type="button" onClick={() => { setEditingId(null); setForm(emptyForm); }} className="secondary-button">{t('cancel')}</button>}
        </div>
      </form>

      <div className="glass grid gap-3 rounded-2xl p-4 md:grid-cols-5">
        <FilterSelect label={t('severity')} value={filters.severity} onChange={(value) => setFilters((current) => ({ ...current, severity: value }))} options={severities} labelFor={(value) => t(`incidentSeverity${value}`)} allLabel={t('allSeverities')} />
        <FilterSelect label={t('status')} value={filters.status} onChange={(value) => setFilters((current) => ({ ...current, status: value }))} options={statuses} labelFor={(value) => t(`incidentStatus${value}`)} allLabel={t('allStatuses')} />
        <FilterSelect label={t('category')} value={filters.category} onChange={(value) => setFilters((current) => ({ ...current, category: value }))} options={categories} labelFor={(value) => t(`incidentCategory${value}`)} allLabel={t('allCategories')} />
        <TextInput label={t('search')} value={filters.search} onChange={(value) => setFilters((current) => ({ ...current, search: value }))} />
        <div className="flex items-end"><button onClick={() => load(filters)} className="primary-button w-full text-sm">{t('applyFilters')}</button></div>
      </div>

      {loading ? <SkeletonCard variant="list" count={5} /> : (
        <div className="grid gap-3">
          {!records.length && <EmptyState icon={ShieldAlert} title={t('incidentsEmpty')} description={t('incidentsEmptyDescription')} />}
          {records.map((incident) => (
            <article key={incident.id} className="glass rounded-2xl p-4">
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div>
                  <div className="mb-2 flex flex-wrap gap-2">
                    <StatusBadge tone={severityTone(incident.severity)}>{t(`incidentSeverity${incident.severity}`)}</StatusBadge>
                    <StatusBadge status={incident.status}>{t(`incidentStatus${incident.status}`)}</StatusBadge>
                    <StatusBadge>{t(`incidentCategory${incident.category}`)}</StatusBadge>
                  </div>
                  <h2 className="text-lg font-semibold">{incident.title}</h2>
                  <p className="mt-2 text-sm text-sky-100/80">{incident.description}</p>
                </div>
                <time className="text-sm text-sky-100/60">{formatDateTime(incident.createdAt, language)}</time>
              </div>
              <div className="mt-3 grid gap-2 text-sm text-sky-100/65 md:grid-cols-3">
                <p>{t('assignedTo')}: {incident.assignedToEmail || t('notAssigned')}</p>
                <p>{t('relatedAuditLogId')}: {incident.relatedAuditLogId || '—'}</p>
                <p>{t('resolvedAt')}: {formatDateTime(incident.resolvedAt, language)}</p>
              </div>
              {incident.resolutionNotes && <p className="mt-3 rounded-xl border border-sky-100/10 bg-sky-950/40 p-3 text-sm text-sky-100/75">{incident.resolutionNotes}</p>}
              <div className="mt-4 flex flex-wrap gap-2">
                <button onClick={() => edit(incident)} className="focus-ring rounded-xl border border-sky-100/20 px-3 py-2 text-sm">{t('edit')}</button>
                {incident.status !== 'INVESTIGATING' && <button disabled={busyId === incident.id} onClick={() => updateStatus(incident, 'INVESTIGATING')} className="focus-ring rounded-xl border border-sky-100/20 px-3 py-2 text-sm disabled:opacity-60">{t('markInvestigating')}</button>}
                {incident.status !== 'RESOLVED' && <button disabled={busyId === incident.id} onClick={() => updateStatus(incident, 'RESOLVED')} className="focus-ring rounded-xl border border-emerald-300/40 px-3 py-2 text-sm text-emerald-100 disabled:opacity-60">{t('markResolved')}</button>}
                {incident.status !== 'FALSE_POSITIVE' && <button disabled={busyId === incident.id} onClick={() => markFalsePositive(incident)} className="focus-ring rounded-xl border border-amber-300/40 px-3 py-2 text-sm text-amber-100 disabled:opacity-60">{t('markFalsePositive')}</button>}
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

function toPayload(form) {
  return {
    title: form.title.trim(),
    description: form.description.trim(),
    severity: form.severity,
    status: form.status || null,
    category: form.category,
    assignedToUserId: form.assignedToUserId ? Number(form.assignedToUserId) : null,
    relatedAuditLogId: form.relatedAuditLogId ? Number(form.relatedAuditLogId) : null,
    resolutionNotes: form.resolutionNotes.trim() || null,
  };
}

function Select({ label, value, onChange, options, labelFor }) {
  return <label className="block text-sm">{label}<select required value={value} onChange={(event) => onChange(event.target.value)} className="field-control">{options.map((option) => <option key={option} value={option}>{labelFor(option)}</option>)}</select></label>;
}

function FilterSelect({ label, value, onChange, options, labelFor, allLabel }) {
  return <label className="block text-sm">{label}<select value={value} onChange={(event) => onChange(event.target.value)} className="field-control"><option value="">{allLabel}</option>{options.map((option) => <option key={option} value={option}>{labelFor(option)}</option>)}</select></label>;
}

function TextInput({ label, value, onChange, type = 'text', required = false, maxLength }) {
  return <label className="block text-sm">{label}<input required={required} maxLength={maxLength} type={type} value={value} onChange={(event) => onChange(event.target.value)} className="field-control" /></label>;
}

function TextArea({ label, value, onChange, required = false }) {
  return <label className="block text-sm md:col-span-3">{label}<textarea required={required} maxLength={3000} value={value} onChange={(event) => onChange(event.target.value)} className="field-control min-h-24" /></label>;
}

function severityTone(severity) {
  if (severity === 'CRITICAL' || severity === 'HIGH') return 'danger';
  if (severity === 'MEDIUM') return 'warning';
  return 'success';
}
