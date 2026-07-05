import { useEffect, useState } from 'react';
import AvatarPreview from '../../components/AvatarPreview.jsx';
import ErrorState from '../../components/ErrorState.jsx';
import LoadingSpinner from '../../components/LoadingSpinner.jsx';
import PageHeader from '../../components/PageHeader.jsx';
import SkeletonCard from '../../components/SkeletonCard.jsx';
import DataClassificationBadge from '../../components/DataClassificationBadge.jsx';
import { residentsApi } from '../../api/residentsApi.js';
import { useLanguage } from '../../context/LanguageContext.jsx';
import { UKRAINIAN_PHONE_PLACEHOLDER, formatUkrainianPhone, isValidUkrainianPhone } from '../../utils/phoneFormat.js';

const emptyForm = {
  phone: '',
  emergencyContactName: '',
  emergencyContactPhone: '',
  preferredLanguage: 'uk',
};

export default function Profile() {
  const { t } = useLanguage();
  const [profile, setProfile] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [errors, setErrors] = useState({});
  const [avatarBusy, setAvatarBusy] = useState(false);

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await residentsApi.getOwnProfile();
      setProfile(data);
      setForm({
        phone: formatUkrainianPhone(data.phone) || data.phone || '',
        emergencyContactName: data.emergencyContactName || '',
        emergencyContactPhone: formatUkrainianPhone(data.emergencyContactPhone) || data.emergencyContactPhone || '',
        preferredLanguage: data.preferredLanguage || 'uk',
      });
    } catch (loadError) {
      setError(loadError.message || t('profileLoadFailed'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const validate = () => {
    const next = {};
    if (form.phone.length > 40) next.phone = t('residentPhoneTooLong');
    if (!isValidUkrainianPhone(form.phone)) next.phone = t('residentPhoneInvalid');
    if (form.emergencyContactName.length > 120) next.emergencyContactName = t('emergencyContactTooLong');
    if (form.emergencyContactPhone.length > 40) next.emergencyContactPhone = t('emergencyPhoneTooLong');
    if (!isValidUkrainianPhone(form.emergencyContactPhone)) next.emergencyContactPhone = t('emergencyPhoneInvalid');
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
      setSuccess(t('profileSaved'));
    } catch (saveError) {
      setError(saveError.message || t('profileSaveFailed'));
    } finally {
      setSaving(false);
    }
  };

  const updateField = (field, value) => {
    const nextValue = field === 'phone' || field === 'emergencyContactPhone'
      ? formatUkrainianPhone(value)
      : value;
    setForm((current) => ({ ...current, [field]: nextValue }));
    setErrors((current) => ({ ...current, [field]: '' }));
  };

  const uploadAvatar = async (event) => {
    const file = event.target.files?.[0];
    event.target.value = '';
    if (!file || avatarBusy) return;
    const validationError = validateAvatarFile(file, t);
    if (validationError) {
      setError(validationError);
      setSuccess('');
      return;
    }
    setAvatarBusy(true);
    setError('');
    setSuccess('');
    try {
      const updated = await residentsApi.uploadOwnAvatar(file);
      setProfile(updated);
      setSuccess(t('avatarUploaded'));
    } catch (uploadError) {
      setError(uploadError.message || t('avatarUploadFailed'));
    } finally {
      setAvatarBusy(false);
    }
  };

  const removeAvatar = async () => {
    if (avatarBusy) return;
    setAvatarBusy(true);
    setError('');
    setSuccess('');
    try {
      const updated = await residentsApi.deleteOwnAvatar();
      setProfile(updated);
      setSuccess(t('avatarRemoved'));
    } catch (removeError) {
      setError(removeError.message || t('avatarRemoveFailed'));
    } finally {
      setAvatarBusy(false);
    }
  };

  if (loading) return <SkeletonCard variant="form" rows={6} />;

  return (
    <section className="space-y-5">
      <PageHeader title={t('profileTitle')} subtitle={t('profileSubtitle')} action={<button onClick={load} className="secondary-button">{t('refresh')}</button>} />
      {error && <ErrorState title={t('errorTitle')} description={error} onRetry={load} retryLabel={t('retry')} />}
      {success && <div className="rounded-xl border border-emerald-300/40 bg-emerald-500/15 p-3 text-sm text-emerald-100">{success}</div>}
      {profile && (
        <div className="grid gap-4 xl:grid-cols-[minmax(0,0.85fr)_minmax(0,1.15fr)]">
          <article className="glass rounded-2xl p-5">
            <div className="flex items-start gap-4">
              <AvatarPreview avatarUrl={profile.avatarUrl} name={profile.name} interactive label={profile.avatarUrl ? t('replaceAvatar') : t('uploadAvatar')} />
              <div className="min-w-0">
                <p className="break-words text-xl font-semibold">{profile.name}</p>
                <p className="break-words text-sm text-sky-100/70">{profile.email}</p>
                <p className="mt-2 text-xs uppercase tracking-wide text-sky-100/55">{profile.role}</p>
              </div>
            </div>
            <div className="mt-5 grid gap-3 text-sm">
              <Info label={t('apartment')} value={profile.apartmentNumber ? `${t('sectionLabel')} ${profile.buildingSection}, ${t('apartmentShort')} ${profile.apartmentNumber}, ${t('floorLabel')} ${profile.floor}` : t('notAssigned')} />
              <Info label={t('yourPhoneNumber')} value={profile.phone || t('notProvided')} confidential />
              <Info label={t('emergencyContactPerson')} value={profile.emergencyContactName || t('notProvided')} confidential />
              <Info label={t('emergencyContactPhone')} value={profile.emergencyContactPhone || t('notProvided')} confidential />
            </div>
            <div className="mt-5 rounded-xl border border-sky-100/15 bg-sky-400/10 p-3 text-xs text-sky-100/70">
              <p className="font-semibold text-sky-50">{t('avatar')}</p>
              <p className="mt-1">{t('avatarRules')}</p>
              <div className="mt-3 flex flex-wrap gap-2">
                <label className="primary-button cursor-pointer text-sm">
                  {avatarBusy ? t('saving') : profile.avatarUrl ? t('replaceAvatar') : t('uploadAvatar')}
                  <input type="file" accept="image/jpeg,image/png,image/webp" onChange={uploadAvatar} className="sr-only" />
                </label>
                {profile.avatarUrl && <button type="button" disabled={avatarBusy} onClick={removeAvatar} className="focus-ring rounded-xl border border-rose-300/40 px-3 py-2 text-sm text-rose-100 disabled:opacity-60">{t('removeAvatar')}</button>}
              </div>
            </div>
          </article>
          <form onSubmit={save} className="glass grid content-start gap-3 rounded-2xl p-5 md:grid-cols-2">
            <Field label={t('yourPhoneNumber')} error={errors.phone}><input value={form.phone} onChange={(e) => updateField('phone', e.target.value)} placeholder={UKRAINIAN_PHONE_PLACEHOLDER} inputMode="tel" className={inputClass(errors.phone)} /></Field>
            <Field label={t('language')}><select value={form.preferredLanguage} onChange={(e) => updateField('preferredLanguage', e.target.value)} className={inputClass()}><option value="uk">Українська</option><option value="en">English</option></select></Field>
            <Field label={t('emergencyContactPerson')} error={errors.emergencyContactName}><input value={form.emergencyContactName} onChange={(e) => updateField('emergencyContactName', e.target.value)} className={inputClass(errors.emergencyContactName)} /></Field>
            <Field label={t('emergencyContactPhone')} error={errors.emergencyContactPhone}><input value={form.emergencyContactPhone} onChange={(e) => updateField('emergencyContactPhone', e.target.value)} placeholder={UKRAINIAN_PHONE_PLACEHOLDER} inputMode="tel" className={inputClass(errors.emergencyContactPhone)} /></Field>
            <button disabled={saving} className="primary-button md:col-span-2">{saving ? <LoadingSpinner label={t('saving')} /> : t('saveProfile')}</button>
          </form>
        </div>
      )}
    </section>
  );
}

function validateAvatarFile(file, t) {
  const allowed = ['image/jpeg', 'image/png', 'image/webp'];
  if (file.size > 2 * 1024 * 1024) return t('avatarFileTooLarge');
  if (!allowed.includes(file.type)) return t('avatarUnsupportedType');
  return '';
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
  return `field-control ${error ? 'field-error border-rose-300' : ''}`;
}
