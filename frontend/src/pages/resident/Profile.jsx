import { useEffect, useState } from 'react';
import { UserCircle } from 'lucide-react';
import LoadingSpinner from '../../components/LoadingSpinner.jsx';
import SkeletonCard from '../../components/SkeletonCard.jsx';
import DataClassificationBadge from '../../components/DataClassificationBadge.jsx';
import { residentsApi } from '../../api/residentsApi.js';

const emptyForm = {
  phone: '',
  emergencyContactName: '',
  emergencyContactPhone: '',
  preferredLanguage: 'uk',
};

export default function Profile() {
  const [profile, setProfile] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [errors, setErrors] = useState({});

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await residentsApi.getOwnProfile();
      setProfile(data);
      setForm({
        phone: data.phone || '',
        emergencyContactName: data.emergencyContactName || '',
        emergencyContactPhone: data.emergencyContactPhone || '',
        preferredLanguage: data.preferredLanguage || 'uk',
      });
    } catch (loadError) {
      setError(loadError.message || 'Не вдалося завантажити профіль.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const validate = () => {
    const next = {};
    if (form.phone.length > 40) next.phone = 'Телефон має містити не більше 40 символів.';
    if (form.emergencyContactName.length > 120) next.emergencyContactName = 'Контакт має містити не більше 120 символів.';
    if (form.emergencyContactPhone.length > 40) next.emergencyContactPhone = 'Телефон контакту має містити не більше 40 символів.';
    setErrors(next);
    return !Object.keys(next).length;
  };

  const save = async (event) => {
    event.preventDefault();
    if (saving || !validate()) return;
    setSaving(true);
    setError('');
    setSuccess('');
    try {
      const updated = await residentsApi.updateOwnProfile({
        phone: form.phone.trim() || null,
        emergencyContactName: form.emergencyContactName.trim() || null,
        emergencyContactPhone: form.emergencyContactPhone.trim() || null,
        preferredLanguage: form.preferredLanguage,
      });
      setProfile(updated);
      setSuccess('Профіль оновлено.');
    } catch (saveError) {
      setError(saveError.message || 'Не вдалося зберегти профіль.');
    } finally {
      setSaving(false);
    }
  };

  const updateField = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }));
    setErrors((current) => ({ ...current, [field]: '' }));
  };

  if (loading) return <SkeletonCard rows={5} />;

  return (
    <section className="space-y-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-3xl font-bold">Профіль</h1>
        <button onClick={load} className="focus-ring rounded-xl border border-sky-100/20 px-4 py-2 text-sm">Оновити</button>
      </div>
      {error && <div className="rounded-xl border border-rose-300/40 bg-rose-500/15 p-3 text-sm text-rose-100">{error}</div>}
      {success && <div className="rounded-xl border border-emerald-300/40 bg-emerald-500/15 p-3 text-sm text-emerald-100">{success}</div>}
      {profile && (
        <div className="grid gap-4 xl:grid-cols-[minmax(0,0.85fr)_minmax(0,1.15fr)]">
          <article className="glass rounded-2xl p-5">
            <div className="flex items-start gap-4">
              <div className="grid h-20 w-20 shrink-0 place-items-center rounded-2xl border border-sky-100/15 bg-sky-400/10">
                {profile.avatarPath ? <img src={profile.avatarPath} alt="" className="h-full w-full rounded-2xl object-cover" /> : <UserCircle size={48} className="text-sky-100/70" />}
              </div>
              <div className="min-w-0">
                <p className="break-words text-xl font-semibold">{profile.name}</p>
                <p className="break-words text-sm text-sky-100/70">{profile.email}</p>
                <p className="mt-2 text-xs uppercase tracking-wide text-sky-100/55">{profile.role}</p>
              </div>
            </div>
            <div className="mt-5 grid gap-3 text-sm">
              <Info label="Квартира" value={profile.apartmentNumber ? `Секція ${profile.buildingSection}, кв. ${profile.apartmentNumber}, поверх ${profile.floor}` : 'Не призначена'} />
              <Info label="Телефон" value={profile.phone || 'Не вказано'} confidential />
              <Info label="Аварійний контакт" value={profile.emergencyContactName || 'Не вказано'} confidential />
              <Info label="Телефон контакту" value={profile.emergencyContactPhone || 'Не вказано'} confidential />
            </div>
            <p className="mt-5 rounded-xl border border-sky-100/15 bg-sky-400/10 p-3 text-xs text-sky-100/70">
              Завантаження аватара буде додано на окремому етапі. Зараз backend зберігає тільки шлях до аватара.
            </p>
          </article>
          <form onSubmit={save} className="glass grid content-start gap-3 rounded-2xl p-5 md:grid-cols-2">
            <Field label="Телефон" error={errors.phone}><input value={form.phone} onChange={(e) => updateField('phone', e.target.value)} className={inputClass(errors.phone)} /></Field>
            <Field label="Мова"><select value={form.preferredLanguage} onChange={(e) => updateField('preferredLanguage', e.target.value)} className={inputClass()}><option value="uk">Українська</option><option value="en">English</option></select></Field>
            <Field label="Аварійний контакт" error={errors.emergencyContactName}><input value={form.emergencyContactName} onChange={(e) => updateField('emergencyContactName', e.target.value)} className={inputClass(errors.emergencyContactName)} /></Field>
            <Field label="Телефон контакту" error={errors.emergencyContactPhone}><input value={form.emergencyContactPhone} onChange={(e) => updateField('emergencyContactPhone', e.target.value)} className={inputClass(errors.emergencyContactPhone)} /></Field>
            <button disabled={saving} className="focus-ring rounded-xl bg-primary px-4 py-3 font-semibold disabled:opacity-60 md:col-span-2">{saving ? <LoadingSpinner label="Збереження" /> : 'Зберегти профіль'}</button>
          </form>
        </div>
      )}
    </section>
  );
}

function Info({ label, value, confidential = false }) {
  return (
    <div className="rounded-xl border border-sky-100/15 bg-sky-950/35 p-3">
      <p className="text-xs text-sky-100/55">{label}</p>
      <p className="break-words text-sky-50">{value} {confidential && <DataClassificationBadge level="Confidential" />}</p>
    </div>
  );
}

function Field({ label, error, children }) {
  return (
    <label className="text-sm">
      {label}
      {children}
      {error && <span className="mt-1 block text-xs normal-case text-rose-200">{error}</span>}
    </label>
  );
}

function inputClass(error) {
  return `focus-ring mt-1 w-full rounded-xl border bg-sky-950/50 px-3 py-2 ${error ? 'field-error border-rose-300' : 'border-sky-100/15'}`;
}
