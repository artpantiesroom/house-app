import { useEffect, useMemo, useState } from 'react';
import { Bell } from 'lucide-react';
import DataClassificationBadge from '../../components/DataClassificationBadge.jsx';
import EmptyState from '../../components/EmptyState.jsx';
import ErrorState from '../../components/ErrorState.jsx';
import LoadingSpinner from '../../components/LoadingSpinner.jsx';
import SkeletonCard from '../../components/SkeletonCard.jsx';
import { announcementsApi } from '../../api/announcementsApi.js';
import { useLanguage } from '../../context/LanguageContext.jsx';
import { formatDateTime } from '../../utils/date.js';

const emptyForm = {
  titleUk: '',
  titleEn: '',
  bodyUk: '',
  bodyEn: '',
  category: 'GENERAL',
  priority: 'NORMAL',
  status: 'DRAFT',
  expiresAt: '',
};

const labels = {
  uk: {
    titleUk: 'Заголовок UK',
    titleEn: 'Заголовок EN',
    bodyUk: 'Текст UK',
    bodyEn: 'Текст EN',
    category: 'Категорія',
    priority: 'Пріоритет',
    status: 'Статус',
    expiresAt: 'Діє до',
    publishedAt: 'Опубліковано',
    create: 'Створити оголошення',
    save: 'Зберегти оголошення',
    cancel: 'Скасувати',
    edit: 'Редагувати',
    publish: 'Опублікувати',
    archive: 'Архівувати',
    deleting: 'Архівування...',
    loadError: 'Не вдалося завантажити оголошення.',
    saveError: 'Не вдалося зберегти оголошення.',
    empty: 'Оголошень ще немає.',
    required: 'Заголовок UK і текст UK обовʼязкові.',
    filters: 'Фільтри',
    all: 'Усі',
  },
  en: {
    titleUk: 'Title UK',
    titleEn: 'Title EN',
    bodyUk: 'Body UK',
    bodyEn: 'Body EN',
    category: 'Category',
    priority: 'Priority',
    status: 'Status',
    expiresAt: 'Expires at',
    publishedAt: 'Published at',
    create: 'Create announcement',
    save: 'Save announcement',
    cancel: 'Cancel',
    edit: 'Edit',
    publish: 'Publish',
    archive: 'Archive',
    deleting: 'Archiving...',
    loadError: 'Could not load announcements.',
    saveError: 'Could not save announcement.',
    empty: 'No announcements yet.',
    required: 'UK title and UK body are required.',
    filters: 'Filters',
    all: 'All',
  },
};

const categoryLabels = {
  GENERAL: { uk: 'Загальне', en: 'General' },
  MAINTENANCE: { uk: 'Обслуговування', en: 'Maintenance' },
  PAYMENT: { uk: 'Оплати', en: 'Payments' },
  SECURITY: { uk: 'Безпека', en: 'Security' },
  EVENT: { uk: 'Подія', en: 'Event' },
  OTHER: { uk: 'Інше', en: 'Other' },
};

const priorityLabels = {
  LOW: { uk: 'Низький', en: 'Low' },
  NORMAL: { uk: 'Звичайний', en: 'Normal' },
  HIGH: { uk: 'Високий', en: 'High' },
  URGENT: { uk: 'Терміновий', en: 'Urgent' },
};

const statusLabels = {
  DRAFT: { uk: 'Чернетка', en: 'Draft' },
  PUBLISHED: { uk: 'Опубліковано', en: 'Published' },
  ARCHIVED: { uk: 'Архів', en: 'Archived' },
};

export default function Announcements() {
  const { language, t } = useLanguage();
  const l = labels[language];
  const [announcements, setAnnouncements] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [filters, setFilters] = useState({ status: '', category: '', priority: '', search: '' });
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [busyId, setBusyId] = useState(null);
  const [error, setError] = useState('');

  const categoryOptions = useMemo(() => Object.keys(categoryLabels), []);
  const priorityOptions = useMemo(() => Object.keys(priorityLabels), []);
  const statusOptions = useMemo(() => Object.keys(statusLabels), []);

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      setAnnouncements(await announcementsApi.listAdmin(filters));
    } catch {
      setError(l.loadError);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const updateField = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }));
    setError('');
  };

  const submit = async (event) => {
    event.preventDefault();
    if (!form.titleUk.trim() || !form.bodyUk.trim()) {
      setError(l.required);
      return;
    }
    setSaving(true);
    setError('');
    try {
      const payload = {
        ...form,
        titleUk: form.titleUk.trim(),
        titleEn: form.titleEn.trim() || null,
        bodyUk: form.bodyUk.trim(),
        bodyEn: form.bodyEn.trim() || null,
        expiresAt: form.expiresAt ? new Date(form.expiresAt).toISOString() : null,
      };
      if (editingId) {
        await announcementsApi.update(editingId, payload);
      } else {
        await announcementsApi.create(payload);
      }
      setForm(emptyForm);
      setEditingId(null);
      await load();
    } catch (err) {
      setError(err.message || l.saveError);
    } finally {
      setSaving(false);
    }
  };

  const edit = (announcement) => {
    setEditingId(announcement.id);
    setForm({
      titleUk: announcement.titleUk || '',
      titleEn: announcement.titleEn || '',
      bodyUk: announcement.bodyUk || '',
      bodyEn: announcement.bodyEn || '',
      category: announcement.category,
      priority: announcement.priority,
      status: announcement.status,
      expiresAt: toDateTimeLocal(announcement.expiresAt),
    });
    setError('');
  };

  const action = async (id, nextAction) => {
    setBusyId(id);
    setError('');
    try {
      if (nextAction === 'publish') await announcementsApi.publish(id);
      if (nextAction === 'archive') await announcementsApi.archive(id);
      if (nextAction === 'remove') await announcementsApi.remove(id);
      await load();
    } catch (err) {
      setError(err.message || l.saveError);
    } finally {
      setBusyId(null);
    }
  };

  return (
    <section className="space-y-5">
      <h1 className="text-3xl font-bold">{t('announcementsTitle')}</h1>

      <form onSubmit={submit} className="glass space-y-4 rounded-2xl p-4">
        <div className="grid gap-3 md:grid-cols-2">
          <TextInput label={l.titleUk} value={form.titleUk} onChange={(value) => updateField('titleUk', value)} required />
          <TextInput label={l.titleEn} value={form.titleEn} onChange={(value) => updateField('titleEn', value)} />
          <TextArea label={l.bodyUk} value={form.bodyUk} onChange={(value) => updateField('bodyUk', value)} required />
          <TextArea label={l.bodyEn} value={form.bodyEn} onChange={(value) => updateField('bodyEn', value)} />
          <Select label={l.category} value={form.category} onChange={(value) => updateField('category', value)} options={categoryOptions} labels={categoryLabels} language={language} />
          <Select label={l.priority} value={form.priority} onChange={(value) => updateField('priority', value)} options={priorityOptions} labels={priorityLabels} language={language} />
          <Select label={l.status} value={form.status} onChange={(value) => updateField('status', value)} options={statusOptions} labels={statusLabels} language={language} />
          <TextInput label={l.expiresAt} type="datetime-local" value={form.expiresAt} onChange={(value) => updateField('expiresAt', value)} />
        </div>
        {error && <ErrorState title={t('errorTitle')} description={error} />}
        <div className="flex flex-wrap gap-2">
          <button disabled={saving} className="primary-button">
            {saving ? <LoadingSpinner label={t('saving')} /> : editingId ? l.save : l.create}
          </button>
          {editingId && <button type="button" onClick={() => { setEditingId(null); setForm(emptyForm); }} className="secondary-button">{l.cancel}</button>}
        </div>
      </form>

      <div className="glass grid gap-3 rounded-2xl p-4 md:grid-cols-4">
        <Select label={l.status} value={filters.status} onChange={(value) => setFilters((current) => ({ ...current, status: value }))} options={['', ...statusOptions]} labels={{ '': { uk: l.all, en: l.all }, ...statusLabels }} language={language} />
        <Select label={l.category} value={filters.category} onChange={(value) => setFilters((current) => ({ ...current, category: value }))} options={['', ...categoryOptions]} labels={{ '': { uk: l.all, en: l.all }, ...categoryLabels }} language={language} />
        <Select label={l.priority} value={filters.priority} onChange={(value) => setFilters((current) => ({ ...current, priority: value }))} options={['', ...priorityOptions]} labels={{ '': { uk: l.all, en: l.all }, ...priorityLabels }} language={language} />
        <div className="flex items-end gap-2">
          <TextInput label={t('search')} value={filters.search} onChange={(value) => setFilters((current) => ({ ...current, search: value }))} />
          <button onClick={load} className="secondary-button px-3 py-2">{t('refresh')}</button>
        </div>
      </div>

      {loading ? <SkeletonCard variant="list" count={4} /> : (
        <div className="grid gap-3 md:grid-cols-2">
          {!announcements.length && <EmptyState icon={Bell} title={l.empty} description={t('emptyAnnouncementsDescription')} />}
          {announcements.map((announcement) => (
            <article key={announcement.id} className="glass rounded-2xl p-4">
              <div className="mb-2 flex items-start justify-between gap-3">
                <div>
                  <h2 className="text-lg font-semibold">{announcement.titleUk}</h2>
                  {announcement.titleEn && <p className="text-sm text-sky-100/55">{announcement.titleEn}</p>}
                </div>
                <DataClassificationBadge level="Public" />
              </div>
              <p className="text-sm text-sky-100/75">{announcement.bodyUk}</p>
              <div className="mt-3 flex flex-wrap gap-2 text-xs">
                <Pill>{categoryLabels[announcement.category]?.[language] || announcement.category}</Pill>
                <Pill>{priorityLabels[announcement.priority]?.[language] || announcement.priority}</Pill>
                <Pill>{statusLabels[announcement.status]?.[language] || announcement.status}</Pill>
              </div>
              <p className="mt-3 text-xs text-sky-100/55">{l.publishedAt}: {formatDateTime(announcement.publishedAt, language)}</p>
              <p className="text-xs text-sky-100/55">{l.expiresAt}: {formatDateTime(announcement.expiresAt, language)}</p>
              <div className="mt-4 flex flex-wrap gap-2">
                <button onClick={() => edit(announcement)} className="focus-ring rounded-xl border border-sky-100/20 px-3 py-2 text-sm">{l.edit}</button>
                {announcement.status !== 'PUBLISHED' && <button disabled={busyId === announcement.id} onClick={() => action(announcement.id, 'publish')} className="focus-ring rounded-xl border border-emerald-300/40 px-3 py-2 text-sm text-emerald-100 disabled:opacity-60">{l.publish}</button>}
                {announcement.status !== 'ARCHIVED' && <button disabled={busyId === announcement.id} onClick={() => action(announcement.id, 'archive')} className="focus-ring rounded-xl border border-amber-300/40 px-3 py-2 text-sm text-amber-100 disabled:opacity-60">{busyId === announcement.id ? l.deleting : l.archive}</button>}
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

function TextInput({ label, value, onChange, type = 'text', required = false }) {
  return <label className="block text-sm">{label}<input required={required} type={type} value={value} onChange={(e) => onChange(e.target.value)} className="field-control" /></label>;
}

function TextArea({ label, value, onChange, required = false }) {
  return <label className="block text-sm">{label}<textarea required={required} value={value} maxLength={5000} onChange={(e) => onChange(e.target.value)} className="field-control min-h-28" /></label>;
}

function Select({ label, value, onChange, options, labels: optionLabels, language }) {
  return <label className="block text-sm">{label}<select value={value} onChange={(e) => onChange(e.target.value)} className="field-control">{options.map((option) => <option key={option} value={option}>{optionLabels[option]?.[language] || option}</option>)}</select></label>;
}

function Pill({ children }) {
  return <span className="rounded-full border border-sky-100/15 bg-sky-950/40 px-2 py-1">{children}</span>;
}

function toDateTimeLocal(value) {
  if (!value) return '';
  const date = new Date(value);
  date.setMinutes(date.getMinutes() - date.getTimezoneOffset());
  return date.toISOString().slice(0, 16);
}
