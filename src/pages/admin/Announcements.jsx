import { useEffect, useState } from 'react';
import DataClassificationBadge from '../../components/DataClassificationBadge.jsx';
import LoadingSpinner from '../../components/LoadingSpinner.jsx';
import SkeletonCard from '../../components/SkeletonCard.jsx';
import { sanitizeText } from '../../data/mockData.js';
import { useAudit } from '../../context/AuditContext.jsx';
import { useAuth } from '../../context/AuthContext.jsx';
import { useData } from '../../context/DataContext.jsx';

export default function Announcements() {
  const data = useData();
  const { user } = useAuth();
  const { appendAuditLog } = useAudit();
  const [ready, setReady] = useState(false);
  const [form, setForm] = useState({ title: '', body: '' });
  const [editingId, setEditingId] = useState('');
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState('');
  useEffect(() => { data.loadPageData(true, 700).then(setReady); }, []);

  const save = async (event) => {
    event.preventDefault();
    if (saving) return;
    if (!sanitizeText(form.title) || !sanitizeText(form.body) || form.body.length > 240) {
      setError('Заголовок і повідомлення обовʼязкові. Повідомлення має містити не більше 240 символів.');
      return;
    }
    setSaving(true);
    const saved = editingId ? await data.editAnnouncement(editingId, { ...data.announcements.find((item) => item.id === editingId), ...form }) : await data.addAnnouncement(form);
    appendAuditLog({ actor: user.email, action: editingId ? 'ANNOUNCEMENT_EDITED' : 'ANNOUNCEMENT_CREATED', target: saved.id, result: 'SUCCESS' });
    setForm({ title: '', body: '' });
    setEditingId('');
    setError('');
    setSaving(false);
  };

  const remove = async (announcement) => {
    if (deletingId) return;
    setDeletingId(announcement.id);
    await data.deleteAnnouncement(announcement.id);
    appendAuditLog({ actor: user.email, action: 'ANNOUNCEMENT_DELETED', target: announcement.id, result: 'SUCCESS' });
    setDeletingId('');
  };

  const updateField = (field, value) => {
    setForm({ ...form, [field]: sanitizeText(value) });
    setError('');
  };

  if (!ready) return <SkeletonCard rows={5} />;
  return (
    <section className="space-y-5">
      <h1 className="text-3xl font-bold">Оголошення</h1>
      <form onSubmit={save} className="glass space-y-3 rounded-2xl p-4">
        <label className="block text-sm">Заголовок<input value={form.title} onChange={(e) => updateField('title', e.target.value)} className={`focus-ring mt-1 w-full rounded-xl border bg-sky-950/50 px-3 py-2 ${error ? 'field-error border-rose-300' : 'border-sky-100/15'}`} /></label>
        <label className="block text-sm">Повідомлення<textarea value={form.body} maxLength={240} onChange={(e) => updateField('body', e.target.value)} className={`focus-ring mt-1 min-h-24 w-full rounded-xl border bg-sky-950/50 px-3 py-2 ${error ? 'field-error border-rose-300' : 'border-sky-100/15'}`} /></label>
        {error && <p className="text-sm text-rose-200">{error}</p>}
        <button disabled={saving} className="focus-ring rounded-xl bg-primary px-4 py-3 font-semibold">{saving ? <LoadingSpinner label="Збереження" /> : editingId ? 'Зберегти оголошення' : 'Створити оголошення'}</button>
      </form>
      <div className="grid gap-3 md:grid-cols-2">
        {data.announcements.map((announcement) => (
          <article key={announcement.id} className="glass rounded-2xl p-4">
            <div className="mb-2 flex items-center justify-between gap-2"><h2 className="text-lg font-semibold">{announcement.title}</h2><DataClassificationBadge level="Public" /></div>
            <p className="text-sm text-sky-100/75">{announcement.body}</p>
            <p className="mt-3 text-xs text-sky-100/55">{announcement.date}</p>
            <div className="mt-4 flex gap-2"><button onClick={() => { setEditingId(announcement.id); setForm({ title: announcement.title, body: announcement.body }); setError(''); }} className="focus-ring rounded-xl border border-sky-100/20 px-3 py-2 text-sm">Редагувати</button><button disabled={deletingId === announcement.id} onClick={() => remove(announcement)} className="focus-ring rounded-xl border border-rose-300/40 px-3 py-2 text-sm text-rose-100 disabled:opacity-50">{deletingId === announcement.id ? 'Видалення...' : 'Видалити'}</button></div>
          </article>
        ))}
      </div>
    </section>
  );
}
