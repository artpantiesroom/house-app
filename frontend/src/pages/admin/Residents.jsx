import { useEffect, useMemo, useState } from 'react';
import DataClassificationBadge from '../../components/DataClassificationBadge.jsx';
import LoadingSpinner from '../../components/LoadingSpinner.jsx';
import PasswordField from '../../components/PasswordField.jsx';
import SkeletonCard from '../../components/SkeletonCard.jsx';
import { apartmentsApi } from '../../api/apartmentsApi.js';
import { residentsApi } from '../../api/residentsApi.js';
import { useAudit } from '../../context/AuditContext.jsx';
import { useAuth } from '../../context/AuthContext.jsx';
import { useLanguage } from '../../context/LanguageContext.jsx';
import { UKRAINIAN_PHONE_PLACEHOLDER, formatUkrainianPhone, isValidUkrainianPhone } from '../../utils/phoneFormat.js';

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
  const { t } = useLanguage();
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
  const [temporaryPasswordVisible, setTemporaryPasswordVisible] = useState(false);

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
      setError(loadError.message || t('residentsLoadFailed'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const validate = () => {
    const next = {};
    if (!form.name.trim()) next.name = t('nameRequired');
    if (form.name.length > 120) next.name = t('nameTooLong');
    if (!emailPattern.test(form.email)) next.email = t('emailInvalid');
    if (!editingId && !passwordPattern.test(form.temporaryPassword)) next.temporaryPassword = t('temporaryPasswordWeak');
    if (form.phone.length > 40) next.phone = t('residentPhoneTooLong');
    if (!isValidUkrainianPhone(form.phone)) next.phone = t('residentPhoneInvalid');
    if (form.emergencyContactName.length > 120) next.emergencyContactName = t('emergencyContactTooLong');
    if (form.emergencyContactPhone.length > 40) next.emergencyContactPhone = t('emergencyPhoneTooLong');
    if (!isValidUkrainianPhone(form.emergencyContactPhone)) next.emergencyContactPhone = t('emergencyPhoneInvalid');
    if (form.notes.length > 1000) next.notes = t('notesTooLong');
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
      setSuccess(editingId ? t('residentUpdated') : t('residentCreatedTemporaryPassword'));
      await load();
    } catch (saveError) {
      setError(saveError.message || t('residentSaveFailed'));
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
      setSuccess(t('residentDeactivated'));
      await load();
    } catch (deleteError) {
      setError(deleteError.message || t('residentDeactivateFailed'));
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
      phone: formatUkrainianPhone(resident.phone) || resident.phone || '',
      apartmentId: resident.apartmentId || '',
      emergencyContactName: resident.emergencyContactName || '',
      emergencyContactPhone: formatUkrainianPhone(resident.emergencyContactPhone) || resident.emergencyContactPhone || '',
      notes: resident.notes || '',
      enabled: Boolean(resident.enabled),
      mustChangePassword: Boolean(resident.mustChangePassword),
      preferredLanguage: resident.preferredLanguage || 'uk',
    });
    setErrors({});
    setSuccess('');
    setTemporaryPasswordVisible(false);
  };

  const updateField = (field, value) => {
    const nextValue = field === 'phone' || field === 'emergencyContactPhone'
      ? formatUkrainianPhone(value)
      : value;
    setForm((current) => ({ ...current, [field]: nextValue }));
    setErrors((current) => ({ ...current, [field]: '' }));
  };

  if (loading) return <SkeletonCard rows={6} />;

  return (
    <section className="space-y-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-3xl font-bold">{t('residentsTitle')}</h1>
        <button onClick={load} className="focus-ring rounded-xl border border-sky-100/20 px-4 py-2 text-sm">{t('refresh')}</button>
      </div>
      {error && <div className="rounded-xl border border-rose-300/40 bg-rose-500/15 p-3 text-sm text-rose-100">{error}</div>}
      {success && <div className="rounded-xl border border-emerald-300/40 bg-emerald-500/15 p-3 text-sm text-emerald-100">{success}</div>}
      <form onSubmit={save} className="glass grid gap-3 rounded-2xl p-4 md:grid-cols-2 xl:grid-cols-4">
        <Field label={t('name')} error={errors.name}><input value={form.name} onChange={(e) => updateField('name', e.target.value)} className={inputClass(errors.name)} /></Field>
        <Field label={t('email')} error={errors.email}><input type="email" value={form.email} onChange={(e) => updateField('email', e.target.value)} className={inputClass(errors.email)} /></Field>
        {!editingId && (
          <Field label={t('temporaryPassword')} error={errors.temporaryPassword}>
            <PasswordField id="temporaryPassword" value={form.temporaryPassword} onChange={(e) => updateField('temporaryPassword', e.target.value)} visible={temporaryPasswordVisible} onToggle={() => setTemporaryPasswordVisible((value) => !value)} showLabel={t('showPassword')} hideLabel={t('hidePassword')} error={errors.temporaryPassword} autoComplete="new-password" />
          </Field>
        )}
        <Field label={t('residentPhone')} error={errors.phone}><input value={form.phone} onChange={(e) => updateField('phone', e.target.value)} placeholder={UKRAINIAN_PHONE_PLACEHOLDER} inputMode="tel" className={inputClass(errors.phone)} /></Field>
        <Field label={t('apartment')}><select value={form.apartmentId} onChange={(e) => updateField('apartmentId', e.target.value)} className={inputClass()}><option value="">{t('noApartment')}</option>{availableApartments.map((apartment) => <option key={apartment.id} value={apartment.id}>{apartment.buildingSection} · {t('apartmentShort')} {apartment.apartmentNumber}, {t('floorLabel')} {apartment.floor}</option>)}</select></Field>
        <Field label={t('emergencyContactPerson')} error={errors.emergencyContactName}><input value={form.emergencyContactName} onChange={(e) => updateField('emergencyContactName', e.target.value)} className={inputClass(errors.emergencyContactName)} /></Field>
        <Field label={t('emergencyContactPhone')} error={errors.emergencyContactPhone}><input value={form.emergencyContactPhone} onChange={(e) => updateField('emergencyContactPhone', e.target.value)} placeholder={UKRAINIAN_PHONE_PLACEHOLDER} inputMode="tel" className={inputClass(errors.emergencyContactPhone)} /></Field>
        <Field label={t('language')}><select value={form.preferredLanguage} onChange={(e) => updateField('preferredLanguage', e.target.value)} className={inputClass()}><option value="uk">Українська</option><option value="en">English</option></select></Field>
        {editingId && (
          <div className="flex flex-wrap items-end gap-4 md:col-span-2 xl:col-span-4">
            <label className="flex items-center gap-2 text-sm"><input type="checkbox" checked={form.enabled} onChange={(e) => updateField('enabled', e.target.checked)} /> {t('active')}</label>
            <label className="flex items-center gap-2 text-sm"><input type="checkbox" checked={form.mustChangePassword} onChange={(e) => updateField('mustChangePassword', e.target.checked)} /> {t('mustChangePassword')}</label>
          </div>
        )}
        <Field label={t('adminNotes')} error={errors.notes} wide><textarea value={form.notes} onChange={(e) => updateField('notes', e.target.value)} rows={3} className={inputClass(errors.notes)} /></Field>
        <div className="flex flex-wrap gap-2 md:col-span-2 xl:col-span-4">
          <button disabled={saving} className="focus-ring rounded-xl bg-primary px-4 py-3 font-semibold disabled:opacity-60">{saving ? <LoadingSpinner label={t('saving')} /> : editingId ? t('saveResident') : t('addResident')}</button>
          {editingId && <button type="button" onClick={() => { setEditingId(''); setForm(emptyForm); setErrors({}); }} className="focus-ring rounded-xl border border-sky-100/20 px-4 py-3 text-sm">{t('cancel')}</button>}
        </div>
      </form>
      {!residents.length ? (
        <div className="glass rounded-2xl p-6 text-sky-100/75">{t('noResidents')}</div>
      ) : (
        <div className="grid gap-3">
          {residents.map((resident) => (
            <article key={resident.id} className="glass rounded-2xl p-4">
              <div className="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
                <div className="min-w-0">
                  <p className="text-lg font-semibold">{resident.name} <DataClassificationBadge level="Internal" /></p>
                  <p className="break-words text-sm text-sky-100/70">{resident.email} · {resident.phone || t('notProvided')}</p>
                  <p className="text-sm text-sky-100/70">{resident.apartmentNumber ? `${t('sectionLabel')} ${resident.buildingSection}, ${t('apartmentShort')} ${resident.apartmentNumber}, ${t('floorLabel')} ${resident.floor}` : t('apartmentNotAssigned')} <DataClassificationBadge level="Confidential" /></p>
                  <div className="mt-2 flex flex-wrap gap-2 text-xs">
                    <span className={`rounded-full px-3 py-1 ${resident.enabled ? 'bg-emerald-400/15 text-emerald-100' : 'bg-rose-400/15 text-rose-100'}`}>{resident.enabled ? t('active') : t('disabled')}</span>
                    <span className={`rounded-full px-3 py-1 ${resident.mustChangePassword ? 'bg-amber-400/15 text-amber-100' : 'bg-sky-400/15 text-sky-100'}`}>{resident.mustChangePassword ? t('mustChangePassword') : t('passwordUpdated')}</span>
                  </div>
                </div>
                <div className="flex shrink-0 flex-wrap gap-2">
                  <button onClick={() => edit(resident)} className="focus-ring rounded-xl border border-sky-100/20 px-3 py-2 text-sm">{t('edit')}</button>
                  <button disabled={deletingId === resident.id || !resident.enabled} onClick={() => deactivate(resident)} className="focus-ring rounded-xl border border-rose-300/40 px-3 py-2 text-sm text-rose-100 disabled:opacity-50">{deletingId === resident.id ? t('deactivating') : t('deactivate')}</button>
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
