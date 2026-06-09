import { useEffect, useState } from 'react';
import DataClassificationBadge from '../../components/DataClassificationBadge.jsx';
import LoadingSpinner from '../../components/LoadingSpinner.jsx';
import SkeletonCard from '../../components/SkeletonCard.jsx';
import { sanitizeText } from '../../data/mockData.js';
import { useAudit } from '../../context/AuditContext.jsx';
import { useAuth } from '../../context/AuthContext.jsx';
import { useData } from '../../context/DataContext.jsx';

const emptyForm = { name: '', email: '', phone: '', apartment: '', floor: 1 };
const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export default function Residents() {
  const data = useData();
  const { user } = useAuth();
  const { appendAuditLog } = useAudit();
  const [ready, setReady] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState('');
  const [errors, setErrors] = useState({});
  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState('');

  useEffect(() => { data.loadPageData(true, 650).then(setReady); }, []);

  const validate = () => {
    const next = {};
    if (!sanitizeText(form.name)) next.name = 'Імʼя обовʼязкове.';
    if (!emailPattern.test(form.email)) next.email = 'Потрібен коректний email.';
    if (!sanitizeText(form.phone)) next.phone = 'Телефон обовʼязковий.';
    if (!sanitizeText(form.apartment)) next.apartment = 'Квартира обовʼязкова.';
    if (String(form.name).length > 80) next.name = 'Імʼя має містити не більше 80 символів.';
    setErrors(next);
    return !Object.keys(next).length;
  };

  const save = async (event) => {
    event.preventDefault();
    if (saving) return;
    if (!validate()) return;
    setSaving(true);
    const saved = editingId ? await data.editResident(editingId, form) : await data.addResident(form);
    appendAuditLog({ actor: user.email, action: editingId ? 'RESIDENT_EDITED' : 'RESIDENT_CREATED', target: saved.id, result: 'SUCCESS' });
    setForm(emptyForm);
    setEditingId('');
    setSaving(false);
  };

  const remove = async (resident) => {
    if (deletingId) return;
    setDeletingId(resident.id);
    await data.deleteResident(resident.id);
    appendAuditLog({ actor: user.email, action: 'RESIDENT_DELETED', target: resident.id, result: 'SUCCESS' });
    setDeletingId('');
  };

  const updateField = (field, value) => {
    setForm({ ...form, [field]: sanitizeText(value) });
    setErrors((current) => ({ ...current, [field]: '' }));
  };

  if (!ready) return <SkeletonCard rows={6} />;

  return (
    <section className="space-y-5">
      <h1 className="text-3xl font-bold">Мешканці</h1>
      <form onSubmit={save} className="glass grid gap-3 rounded-2xl p-4 md:grid-cols-5">
        {[
          ['name', 'Імʼя'],
          ['email', 'Email'],
          ['phone', 'Телефон'],
          ['apartment', 'Квартира'],
        ].map(([field, label]) => (
          <label key={field} className="text-sm">{label}<input value={form[field]} onChange={(e) => updateField(field, e.target.value)} className={`focus-ring mt-1 w-full rounded-xl border bg-sky-950/50 px-3 py-2 ${errors[field] ? 'field-error border-rose-300' : 'border-sky-100/15'}`} />{errors[field] && <span className="text-xs normal-case text-rose-200">{errors[field]}</span>}</label>
        ))}
        <label className="text-sm">Поверх<input type="number" min="1" max="3" value={form.floor} onChange={(e) => setForm({ ...form, floor: e.target.value })} className="focus-ring mt-1 w-full rounded-xl border border-sky-100/15 bg-sky-950/50 px-3 py-2" /></label>
        <button disabled={saving} className="focus-ring rounded-xl bg-primary px-4 py-3 font-semibold md:col-span-5">{saving ? <LoadingSpinner label="Збереження" /> : editingId ? 'Зберегти мешканця' : 'Додати мешканця'}</button>
      </form>
      <div className="grid gap-3">
        {data.residents.map((resident) => (
          <article key={resident.id} className="glass rounded-2xl p-4">
            <div className="flex flex-wrap items-start justify-between gap-3">
              <div><p className="text-lg font-semibold">{resident.name} <DataClassificationBadge level="Internal" /></p><p className="text-sm text-sky-100/70">Кв. {resident.apartment}, поверх {resident.floor} <DataClassificationBadge level="Confidential" /></p><p className="text-sm text-sky-100/70">{resident.email} · {resident.phone}</p></div>
              <div className="flex gap-2"><button onClick={() => { setEditingId(resident.id); setForm(resident); setErrors({}); }} className="focus-ring rounded-xl border border-sky-100/20 px-3 py-2 text-sm">Редагувати</button><button disabled={deletingId === resident.id} onClick={() => remove(resident)} className="focus-ring rounded-xl border border-rose-300/40 px-3 py-2 text-sm text-rose-100 disabled:opacity-50">{deletingId === resident.id ? 'Видалення...' : 'Видалити'}</button></div>
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}
