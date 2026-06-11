import { useEffect, useMemo, useState } from 'react';
import DataClassificationBadge from '../../components/DataClassificationBadge.jsx';
import LoadingSpinner from '../../components/LoadingSpinner.jsx';
import SkeletonCard from '../../components/SkeletonCard.jsx';
import { apartmentsApi } from '../../api/apartmentsApi.js';
import { residentsApi } from '../../api/residentsApi.js';
import { useAudit } from '../../context/AuditContext.jsx';
import { useAuth } from '../../context/AuthContext.jsx';

const emptyForm = {
  name: '',
  email: '',
  temporaryPassword: '',
  phone: '',
  apartmentId: '',
  emergencyContactName: '',
  emergencyContactPhone: '',
  notes: '',
  enabled: true,
  mustChangePassword: true,
  preferredLanguage: 'uk',
};
const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/;

export default function Residents() {
  const { user } = useAuth();
  const { appendAuditLog } = useAudit();
  const [residents, setResidents] = useState([]);
  const [apartments, setApartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState('');
  const [errors, setErrors] = useState({});
  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState('');

  const availableApartments = useMemo(() => {
    const currentApartmentId = editingId ? Number(form.apartmentId || 0) : 0;
    const occupied = new Set(residents.map((resident) => resident.apartmentId).filter(Boolean));
    return apartments.filter((apartment) => !occupied.has(apartment.id) || apartment.id === currentApartmentId);
  }, [apartments, residents, editingId, form.apartmentId]);

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const [residentData, apartmentData] = await Promise.all([residentsApi.list(), apartmentsApi.list()]);
      setResidents(residentData);
      setApartments(apartmentData);
    } catch (loadError) {
      setError(loadError.message || 'Не вдалося завантажити мешканців.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const validate = () => {
    const next = {};
    if (!form.name.trim()) next.name = 'Імʼя обовʼязкове.';
    if (form.name.length > 120) next.name = 'Імʼя має містити не більше 120 символів.';
    if (!emailPattern.test(form.email)) next.email = 'Потрібен коректний email.';
    if (!editingId && !passwordPattern.test(form.temporaryPassword)) next.temporaryPassword = 'Тимчасовий пароль має містити 8+ символів, великі/малі літери, цифру і спецсимвол.';
    if (form.phone.length > 40) next.phone = 'Телефон має містити не більше 40 символів.';
    if (form.emergencyContactName.length > 120) next.emergencyContactName = 'Контакт має містити не більше 120 символів.';
    if (form.emergencyContactPhone.length > 40) next.emergencyContactPhone = 'Телефон контакту має містити не більше 40 символів.';
    if (form.notes.length > 1000) next.notes = 'Нотатки мають містити не більше 1000 символів.';
    setErrors(next);
    return !Object.keys(next).length;
  };

  const payload = () => ({
    name: form.name.trim(),
    email: form.email.trim().toLowerCase(),
    apartmentId: form.apartmentId ? Number(form.apartmentId) : null,
    phone: form.phone.trim() || null,
    emergencyContactName: form.emergencyContactName.trim() || null,
    emergencyContactPhone: form.emergencyContactPhone.trim() || null,
    notes: form.notes.trim() || null,
    enabled: form.enabled,
    mustChangePassword: form.mustChangePassword,
    preferredLanguage: form.preferredLanguage,
  });

  const save = async (event) => {
    event.preventDefault();
    if (saving || !validate()) return;
    setSaving(true);
    setError('');
    setSuccess('');
    try {
      const saved = editingId
        ? await residentsApi.update(editingId, payload())
        : await residentsApi.create({ ...payload(), temporaryPassword: form.temporaryPassword });
      appendAuditLog({ actor: user.email, action: editingId ? 'RESIDENT_EDITED' : 'RESIDENT_CREATED', target: saved.id, result: 'SUCCESS' });
      setForm(emptyForm);
      setEditingId('');
      setSuccess(editingId ? 'Мешканця оновлено.' : 'Мешканця створено. Тимчасовий пароль не зберігається і не повертається API.');
      await load();
    } catch (saveError) {
      setError(saveError.message || 'Не вдалося зберегти мешканця.');
    } finally {
      setSaving(false);
    }
  };

  const deactivate = async (resident) => {
    if (deletingId) return;
    setDeletingId(resident.id);
    setError('');
    setSuccess('');
    try {
      await residentsApi.deactivate(resident.id);
      appendAuditLog({ actor: user.email, action: 'RESIDENT_DEACTIVATED', target: resident.id, result: 'SUCCESS' });
      setSuccess('Мешканця деактивовано.');
      await load();
    } catch (deleteError) {
      setError(deleteError.message || 'Не вдалося деактивувати мешканця.');
    } finally {
      setDeletingId('');
    }
  };

  const edit = (resident) => {
    setEditingId(resident.id);
    setForm({
      name: resident.name || '',
      email: resident.email || '',
      temporaryPassword: '',
      phone: resident.phone || '',
      apartmentId: resident.apartmentId || '',
      emergencyContactName: resident.emergencyContactName || '',
      emergencyContactPhone: resident.emergencyContactPhone || '',
      notes: resident.notes || '',
      enabled: Boolean(resident.enabled),
      mustChangePassword: Boolean(resident.mustChangePassword),
      preferredLanguage: resident.preferredLanguage || 'uk',
    });
    setErrors({});
    setSuccess('');
  };

  const updateField = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }));
    setErrors((current) => ({ ...current, [field]: '' }));
  };

  if (loading) return <SkeletonCard rows={6} />;

  return (
    <section className="space-y-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-3xl font-bold">Мешканці</h1>
        <button onClick={load} className="focus-ring rounded-xl border border-sky-100/20 px-4 py-2 text-sm">Оновити</button>
      </div>
      {error && <div className="rounded-xl border border-rose-300/40 bg-rose-500/15 p-3 text-sm text-rose-100">{error}</div>}
      {success && <div className="rounded-xl border border-emerald-300/40 bg-emerald-500/15 p-3 text-sm text-emerald-100">{success}</div>}
      <form onSubmit={save} className="glass grid gap-3 rounded-2xl p-4 md:grid-cols-2 xl:grid-cols-4">
        <Field label="Імʼя" error={errors.name}><input value={form.name} onChange={(e) => updateField('name', e.target.value)} className={inputClass(errors.name)} /></Field>
        <Field label="Email" error={errors.email}><input type="email" value={form.email} onChange={(e) => updateField('email', e.target.value)} className={inputClass(errors.email)} /></Field>
        {!editingId && <Field label="Тимчасовий пароль" error={errors.temporaryPassword}><input type="password" value={form.temporaryPassword} onChange={(e) => updateField('temporaryPassword', e.target.value)} className={inputClass(errors.temporaryPassword)} /></Field>}
        <Field label="Телефон" error={errors.phone}><input value={form.phone} onChange={(e) => updateField('phone', e.target.value)} className={inputClass(errors.phone)} /></Field>
        <Field label="Квартира"><select value={form.apartmentId} onChange={(e) => updateField('apartmentId', e.target.value)} className={inputClass()}><option value="">Без квартири</option>{availableApartments.map((apartment) => <option key={apartment.id} value={apartment.id}>{apartment.buildingSection} · кв. {apartment.apartmentNumber}, поверх {apartment.floor}</option>)}</select></Field>
        <Field label="Аварійний контакт" error={errors.emergencyContactName}><input value={form.emergencyContactName} onChange={(e) => updateField('emergencyContactName', e.target.value)} className={inputClass(errors.emergencyContactName)} /></Field>
        <Field label="Телефон контакту" error={errors.emergencyContactPhone}><input value={form.emergencyContactPhone} onChange={(e) => updateField('emergencyContactPhone', e.target.value)} className={inputClass(errors.emergencyContactPhone)} /></Field>
        <Field label="Мова"><select value={form.preferredLanguage} onChange={(e) => updateField('preferredLanguage', e.target.value)} className={inputClass()}><option value="uk">Українська</option><option value="en">English</option></select></Field>
        {editingId && (
          <div className="flex flex-wrap items-end gap-4 md:col-span-2 xl:col-span-4">
            <label className="flex items-center gap-2 text-sm"><input type="checkbox" checked={form.enabled} onChange={(e) => updateField('enabled', e.target.checked)} /> Активний</label>
            <label className="flex items-center gap-2 text-sm"><input type="checkbox" checked={form.mustChangePassword} onChange={(e) => updateField('mustChangePassword', e.target.checked)} /> Потрібна зміна пароля</label>
          </div>
        )}
        <Field label="Адміністративні нотатки" error={errors.notes} wide><textarea value={form.notes} onChange={(e) => updateField('notes', e.target.value)} rows={3} className={inputClass(errors.notes)} /></Field>
        <div className="flex flex-wrap gap-2 md:col-span-2 xl:col-span-4">
          <button disabled={saving} className="focus-ring rounded-xl bg-primary px-4 py-3 font-semibold disabled:opacity-60">{saving ? <LoadingSpinner label="Збереження" /> : editingId ? 'Зберегти мешканця' : 'Додати мешканця'}</button>
          {editingId && <button type="button" onClick={() => { setEditingId(''); setForm(emptyForm); setErrors({}); }} className="focus-ring rounded-xl border border-sky-100/20 px-4 py-3 text-sm">Скасувати</button>}
        </div>
      </form>
      {!residents.length ? (
        <div className="glass rounded-2xl p-6 text-sky-100/75">Мешканців ще не додано.</div>
      ) : (
        <div className="grid gap-3">
          {residents.map((resident) => (
            <article key={resident.id} className="glass rounded-2xl p-4">
              <div className="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
                <div className="min-w-0">
                  <p className="text-lg font-semibold">{resident.name} <DataClassificationBadge level="Internal" /></p>
                  <p className="break-words text-sm text-sky-100/70">{resident.email} · {resident.phone || 'телефон не вказано'}</p>
                  <p className="text-sm text-sky-100/70">{resident.apartmentNumber ? `Секція ${resident.buildingSection}, кв. ${resident.apartmentNumber}, поверх ${resident.floor}` : 'Квартира не призначена'} <DataClassificationBadge level="Confidential" /></p>
                  <div className="mt-2 flex flex-wrap gap-2 text-xs">
                    <span className={`rounded-full px-3 py-1 ${resident.enabled ? 'bg-emerald-400/15 text-emerald-100' : 'bg-rose-400/15 text-rose-100'}`}>{resident.enabled ? 'Активний' : 'Вимкнений'}</span>
                    <span className={`rounded-full px-3 py-1 ${resident.mustChangePassword ? 'bg-amber-400/15 text-amber-100' : 'bg-sky-400/15 text-sky-100'}`}>{resident.mustChangePassword ? 'Потрібна зміна пароля' : 'Пароль оновлено'}</span>
                  </div>
                </div>
                <div className="flex shrink-0 flex-wrap gap-2">
                  <button onClick={() => edit(resident)} className="focus-ring rounded-xl border border-sky-100/20 px-3 py-2 text-sm">Редагувати</button>
                  <button disabled={deletingId === resident.id || !resident.enabled} onClick={() => deactivate(resident)} className="focus-ring rounded-xl border border-rose-300/40 px-3 py-2 text-sm text-rose-100 disabled:opacity-50">{deletingId === resident.id ? 'Вимкнення...' : 'Деактивувати'}</button>
                </div>
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

function Field({ label, error, wide = false, children }) {
  return (
    <label className={`text-sm ${wide ? 'md:col-span-2 xl:col-span-4' : ''}`}>
      {label}
      {children}
      {error && <span className="mt-1 block text-xs normal-case text-rose-200">{error}</span>}
    </label>
  );
}

function inputClass(error) {
  return `focus-ring mt-1 w-full rounded-xl border bg-sky-950/50 px-3 py-2 ${error ? 'field-error border-rose-300' : 'border-sky-100/15'}`;
}
