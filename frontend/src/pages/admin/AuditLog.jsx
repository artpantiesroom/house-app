import { useEffect, useState } from 'react';
import { FileClock } from 'lucide-react';
import { auditApi } from '../../api/auditApi.js';
import EmptyState from '../../components/EmptyState.jsx';
import ErrorState from '../../components/ErrorState.jsx';
import PageHeader from '../../components/PageHeader.jsx';
import SkeletonCard from '../../components/SkeletonCard.jsx';
import { useLanguage } from '../../context/LanguageContext.jsx';
import { formatDateTime } from '../../utils/date.js';

const auditActions = [
  'LOGIN_SUCCESS', 'LOGIN_FAILED', 'LOGOUT', 'PASSWORD_CHANGED',
  'RESIDENT_CREATED', 'RESIDENT_UPDATED', 'RESIDENT_DEACTIVATED',
  'APARTMENT_CREATED', 'APARTMENT_UPDATED',
  'ANNOUNCEMENT_CREATED', 'ANNOUNCEMENT_PUBLISHED', 'ANNOUNCEMENT_ARCHIVED',
  'CONTACT_CREATED', 'CONTACT_UPDATED', 'CONTACT_DEACTIVATED',
  'MAINTENANCE_CREATED', 'MAINTENANCE_UPDATED',
  'PAYMENT_CREATED', 'PAYMENT_UPDATED', 'PAYMENT_STATUS_CHANGED', 'PAYMENT_CANCELLED',
  'SECURITY_INCIDENT_CREATED', 'SECURITY_INCIDENT_UPDATED', 'SECURITY_INCIDENT_RESOLVED',
];

const entityTypes = ['AUTH', 'USER', 'RESIDENT', 'APARTMENT', 'ANNOUNCEMENT', 'CONTACT', 'MAINTENANCE_REQUEST', 'PAYMENT', 'SECURITY_INCIDENT', 'SYSTEM'];

export default function AuditLog() {
  const { language, t } = useLanguage();
  const [records, setRecords] = useState([]);
  const [filters, setFilters] = useState({ action: '', entityType: '', search: '', from: '', to: '' });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = async (nextFilters = filters) => {
    setLoading(true);
    setError('');
    try {
      const payload = {
        action: nextFilters.action,
        entityType: nextFilters.entityType,
        search: nextFilters.search,
        dateFrom: nextFilters.from ? `${nextFilters.from}T00:00:00Z` : '',
        dateTo: nextFilters.to ? `${nextFilters.to}T23:59:59Z` : '',
      };
      setRecords(await auditApi.list(payload));
    } catch (err) {
      setError(err.message || t('auditLoadFailed'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  return (
    <section className="space-y-5">
      <PageHeader title={t('auditLogTitle')} subtitle={t('auditLogSubtitle')} />

      <div className="glass grid gap-3 rounded-2xl p-4 md:grid-cols-6">
        <Select label={t('auditAction')} value={filters.action} onChange={(value) => setFilters((current) => ({ ...current, action: value }))} options={auditActions} labelFor={(value) => t(`auditAction${value}`)} allLabel={t('allActions')} />
        <Select label={t('auditEntityType')} value={filters.entityType} onChange={(value) => setFilters((current) => ({ ...current, entityType: value }))} options={entityTypes} labelFor={(value) => t(`auditEntity${value}`)} allLabel={t('allEntities')} />
        <TextInput label={t('search')} value={filters.search} onChange={(value) => setFilters((current) => ({ ...current, search: value }))} />
        <TextInput label={t('dateFrom')} type="date" value={filters.from} onChange={(value) => setFilters((current) => ({ ...current, from: value }))} />
        <TextInput label={t('dateTo')} type="date" value={filters.to} onChange={(value) => setFilters((current) => ({ ...current, to: value }))} />
        <div className="flex items-end"><button onClick={() => load(filters)} className="primary-button w-full text-sm">{t('applyFilters')}</button></div>
      </div>

      {error && <ErrorState title={t('errorTitle')} description={error} onRetry={() => load(filters)} retryLabel={t('retry')} />}
      {loading ? <SkeletonCard variant="list" count={5} /> : (
        <div className="grid gap-3">
          {!records.length && <EmptyState icon={FileClock} title={t('auditEmpty')} description={t('auditEmptyDescription')} />}
          {records.map((entry) => (
            <article key={entry.id} className="glass rounded-2xl p-4">
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div>
                  <p className="font-semibold">{entry.actorEmail || t('systemActor')} <span className="text-xs text-sky-100/55">{entry.actorRole || 'SYSTEM'}</span></p>
                  <p className="mt-1 text-sm text-sky-100/70">{t(`auditAction${entry.action}`)} · {t(`auditEntity${entry.entityType}`)}{entry.entityId ? ` #${entry.entityId}` : ''}</p>
                </div>
                <time className="text-sm text-sky-100/65">{formatDateTime(entry.createdAt, language)}</time>
              </div>
              <p className="mt-3 text-sm text-sky-100/85">{entry.summary}</p>
              {entry.metadataJson && <pre className="mt-3 overflow-x-auto rounded-xl border border-sky-100/10 bg-sky-950/50 p-3 text-xs text-sky-100/70">{formatJson(entry.metadataJson)}</pre>}
              {(entry.ipAddress || entry.userAgent) && <p className="mt-3 text-xs text-sky-100/45">{entry.ipAddress || '—'} · {entry.userAgent || '—'}</p>}
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

function Select({ label, value, onChange, options, labelFor, allLabel }) {
  return <label className="block text-sm">{label}<select value={value} onChange={(event) => onChange(event.target.value)} className="field-control"><option value="">{allLabel}</option>{options.map((option) => <option key={option} value={option}>{labelFor(option)}</option>)}</select></label>;
}

function TextInput({ label, value, onChange, type = 'text' }) {
  return <label className="block text-sm">{label}<input type={type} value={value} onChange={(event) => onChange(event.target.value)} className="field-control" /></label>;
}

function formatJson(value) {
  try {
    return JSON.stringify(JSON.parse(value), null, 2);
  } catch {
    return value;
  }
}
