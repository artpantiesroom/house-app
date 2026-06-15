import { useEffect, useState } from 'react';
import LoadingSpinner from '../../components/LoadingSpinner.jsx';
import DataClassificationBadge from '../../components/DataClassificationBadge.jsx';
import SkeletonCard from '../../components/SkeletonCard.jsx';
import StatusBadge from '../../components/StatusBadge.jsx';
import { sanitizeText } from '../../data/mockData.js';
import { useAudit } from '../../context/AuditContext.jsx';
import { useAuth } from '../../context/AuthContext.jsx';
import { useData } from '../../context/DataContext.jsx';
import { useLanguage } from '../../context/LanguageContext.jsx';

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

export function getRequestCategoryLabel(category, t) {
  const normalized = String(category || '').trim().toUpperCase();
  const legacy = {
    'САНТЕХНІКА': 'plumbing',
    'ЕЛЕКТРИКА': 'electricity',
    'КЛІМАТ-СИСТЕМА': 'heating',
    'ДОСТУП': 'security',
    PLUMBING: 'plumbing',
    ELECTRICITY: 'electricity',
    HEATING: 'heating',
    INTERNET: 'internet',
    ELEVATOR: 'elevator',
    CLEANING: 'cleaning',
    SECURITY: 'security',
    OTHER: 'other',
  };
  return legacy[normalized] ? t(legacy[normalized]) : category || t('other');
}

export default function MyRequests() {
  const data = useData();
  const { user } = useAuth();
  const { t } = useLanguage();
  const { appendAuditLog } = useAudit();
  const [ready, setReady] = useState(false);
  const [form, setForm] = useState({ title: '', category: '', details: '' });
  const [errors, setErrors] = useState({});
  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState('');
  useEffect(() => { data.loadPageData(true, 800).then(setReady); }, []);
  const currentResidentId = user.residentId || user.id;
  const myRequests = data.requests.filter((request) => request.residentId === currentResidentId);

  const validate = () => {
    const next = {};
    if (!sanitizeText(form.title)) next.title = t('titleRequired');
    if (!sanitizeText(form.category)) next.category = t('categoryRequired');
    if (!sanitizeText(form.details)) next.details = t('detailsRequired');
    if (form.details.length > 280) next.details = t('detailsTooLong');
    setErrors(next);
    return !Object.keys(next).length;
  };

  const submit = async (event) => {
    event.preventDefault();
    if (saving) return;
    if (!validate()) return;
    setSaving(true);
    const created = await data.createRequest({ ...form, residentId: currentResidentId });
    appendAuditLog({ actor: user.email, action: 'REQUEST_CREATED', target: created.id, result: 'SUCCESS' });
    setForm({ title: '', category: '', details: '' });
    setSuccess(t('requestSent'));
    setSaving(false);
  };

  const updateField = (field, value) => {
    setForm({ ...form, [field]: sanitizeText(value) });
    setErrors((current) => ({ ...current, [field]: '' }));
    setSuccess('');
  };

  if (!ready) return <SkeletonCard rows={6} />;
  return (
    <section className="space-y-5">
      <h1 className="text-3xl font-bold">{t('myRequests')}</h1>
      <form onSubmit={submit} className="glass space-y-3 rounded-2xl p-4">
        <label className="block text-sm">{t('title')}<input value={form.title} onChange={(e) => updateField('title', e.target.value)} className={`focus-ring mt-1 w-full rounded-xl border bg-sky-950/50 px-3 py-2 ${errors.title ? 'field-error border-rose-300' : 'border-sky-100/15'}`} />{errors.title && <span className="text-xs normal-case text-rose-200">{errors.title}</span>}</label>
        <label className="block text-sm">{t('category')}<select value={form.category} onChange={(e) => updateField('category', e.target.value)} className={`focus-ring mt-1 w-full rounded-xl border bg-sky-950/50 px-3 py-2 ${errors.category ? 'field-error border-rose-300' : 'border-sky-100/15'}`}><option value=""></option>{requestCategories.map(([value, labelKey]) => <option key={value} value={value}>{t(labelKey)}</option>)}</select>{errors.category && <span className="text-xs normal-case text-rose-200">{errors.category}</span>}</label>
        <label className="block text-sm">{t('details')}<textarea maxLength={280} value={form.details} onChange={(e) => updateField('details', e.target.value)} className={`focus-ring mt-1 min-h-24 w-full rounded-xl border bg-sky-950/50 px-3 py-2 ${errors.details ? 'field-error border-rose-300' : 'border-sky-100/15'}`} />{errors.details && <span className="text-xs text-rose-200">{errors.details}</span>}</label>
        {success && <p className="rounded-xl border border-emerald-300/40 bg-emerald-400/10 p-3 text-sm text-emerald-100">{success}</p>}
        <button disabled={saving} className="focus-ring rounded-xl bg-primary px-4 py-3 font-semibold">{saving ? <LoadingSpinner label={t('sending')} /> : t('submitRequest')}</button>
      </form>
      <div className="grid gap-3">
        {myRequests.length ? myRequests.map((request) => <article key={request.id} className="glass rounded-2xl p-4"><div className="flex items-center justify-between gap-3"><p className="font-semibold">{request.title}</p><StatusBadge status={request.status} /></div><p className="mt-1 text-xs text-sky-100/55">{getRequestCategoryLabel(request.category, t)}</p><p className="mt-2 text-sm text-sky-100/75">{request.details} <DataClassificationBadge level="Internal" /></p></article>) : <div className="glass rounded-2xl p-5 text-sky-100/70">{t('noRequests')}</div>}
      </div>
    </section>
  );
}
