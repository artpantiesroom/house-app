import { useEffect, useState } from 'react';
import { Building2 } from 'lucide-react';
import DataClassificationBadge from '../../components/DataClassificationBadge.jsx';
import EmptyState from '../../components/EmptyState.jsx';
import ErrorState from '../../components/ErrorState.jsx';
import LoadingSpinner from '../../components/LoadingSpinner.jsx';
import PageHeader from '../../components/PageHeader.jsx';
import SkeletonCard from '../../components/SkeletonCard.jsx';
import { contactsApi } from '../../api/contactsApi.js';
import { useLanguage } from '../../context/LanguageContext.jsx';
import { formatUkrainianPhone, isValidUkrainianPhone, UKRAINIAN_PHONE_PLACEHOLDER } from '../../utils/phoneFormat.js';

const emptyForm = {
  nameUk: '',
  nameEn: '',
  roleUk: '',
  roleEn: '',
  departmentUk: '',
  departmentEn: '',
  phone: '',
  email: '',
  availabilityUk: '',
  availabilityEn: '',
  sortOrder: 0,
  active: true,
};

const labels = {
  uk: {
    nameUk: 'Назва UK',
    nameEn: 'Назва EN',
    roleUk: 'Роль UK',
    roleEn: 'Роль EN',
    departmentUk: 'Відділ UK',
    departmentEn: 'Відділ EN',
    phone: 'Телефон',
    email: 'Email',
    availabilityUk: 'Доступність UK',
    availabilityEn: 'Доступність EN',
    sortOrder: 'Порядок',
    active: 'Активний',
    create: 'Створити контакт',
    save: 'Зберегти контакт',
    cancel: 'Скасувати',
    edit: 'Редагувати',
    deactivate: 'Деактивувати',
    loadError: 'Не вдалося завантажити контакти.',
    saveError: 'Не вдалося зберегти контакт.',
    empty: 'Контактів ще немає.',
    required: 'Назва UK, роль UK і телефон або email обовʼязкові.',
    invalidPhone: 'Введіть телефон у форматі +38(067)-123-45-67.',
  },
  en: {
    nameUk: 'Name UK',
    nameEn: 'Name EN',
    roleUk: 'Role UK',
    roleEn: 'Role EN',
    departmentUk: 'Department UK',
    departmentEn: 'Department EN',
    phone: 'Phone',
    email: 'Email',
    availabilityUk: 'Availability UK',
    availabilityEn: 'Availability EN',
    sortOrder: 'Order',
    active: 'Active',
    create: 'Create contact',
    save: 'Save contact',
    cancel: 'Cancel',
    edit: 'Edit',
    deactivate: 'Deactivate',
    loadError: 'Could not load contacts.',
    saveError: 'Could not save contact.',
    empty: 'No contacts yet.',
    required: 'UK name, UK role, and phone or email are required.',
    invalidPhone: 'Enter the phone number as +38(067)-123-45-67.',
  },
};

export default function Contacts() {
  const { language, t } = useLanguage();
  const l = labels[language];
  const [contacts, setContacts] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [busyId, setBusyId] = useState(null);
  const [error, setError] = useState('');

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      setContacts(await contactsApi.listAdmin());
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
    if (!form.nameUk.trim() || !form.roleUk.trim() || (!form.phone.trim() && !form.email.trim())) {
      setError(l.required);
      return;
    }
    if (!isValidUkrainianPhone(form.phone)) {
      setError(l.invalidPhone);
      return;
    }
    setSaving(true);
    setError('');
    try {
      const payload = {
        ...form,
        nameUk: form.nameUk.trim(),
        nameEn: form.nameEn.trim() || null,
        roleUk: form.roleUk.trim(),
        roleEn: form.roleEn.trim() || null,
        departmentUk: form.departmentUk.trim() || null,
        departmentEn: form.departmentEn.trim() || null,
        phone: form.phone.trim() || null,
        email: form.email.trim() || null,
        availabilityUk: form.availabilityUk.trim() || null,
        availabilityEn: form.availabilityEn.trim() || null,
        sortOrder: Number(form.sortOrder) || 0,
        active: Boolean(form.active),
      };
      if (editingId) {
        await contactsApi.update(editingId, payload);
      } else {
        await contactsApi.create(payload);
      }
      setEditingId(null);
      setForm(emptyForm);
      await load();
    } catch (err) {
      setError(err.message || l.saveError);
    } finally {
      setSaving(false);
    }
  };

  const edit = (contact) => {
    setEditingId(contact.id);
    setForm({
      nameUk: contact.nameUk || '',
      nameEn: contact.nameEn || '',
      roleUk: contact.roleUk || '',
      roleEn: contact.roleEn || '',
      departmentUk: contact.departmentUk || '',
      departmentEn: contact.departmentEn || '',
      phone: contact.phone || '',
      email: contact.email || '',
      availabilityUk: contact.availabilityUk || '',
      availabilityEn: contact.availabilityEn || '',
      sortOrder: contact.sortOrder || 0,
      active: Boolean(contact.active),
    });
    setError('');
  };

  const deactivate = async (id) => {
    setBusyId(id);
    setError('');
    try {
      await contactsApi.deactivate(id);
      await load();
    } catch (err) {
      setError(err.message || l.saveError);
    } finally {
      setBusyId(null);
    }
  };

  return (
    <section className="space-y-5">
      <PageHeader title={t('buildingContactsTitle')} subtitle={t('contactsSubtitle')} />

      <form onSubmit={submit} className="glass space-y-4 rounded-2xl p-4">
        <div className="grid gap-3 md:grid-cols-2">
          <TextInput label={l.nameUk} value={form.nameUk} onChange={(value) => updateField('nameUk', value)} required />
          <TextInput label={l.nameEn} value={form.nameEn} onChange={(value) => updateField('nameEn', value)} />
          <TextInput label={l.roleUk} value={form.roleUk} onChange={(value) => updateField('roleUk', value)} required />
          <TextInput label={l.roleEn} value={form.roleEn} onChange={(value) => updateField('roleEn', value)} />
          <TextInput label={l.departmentUk} value={form.departmentUk} onChange={(value) => updateField('departmentUk', value)} />
          <TextInput label={l.departmentEn} value={form.departmentEn} onChange={(value) => updateField('departmentEn', value)} />
          <TextInput label={l.phone} value={form.phone} placeholder={UKRAINIAN_PHONE_PLACEHOLDER} onChange={(value) => updateField('phone', formatUkrainianPhone(value))} />
          <TextInput label={l.email} type="email" value={form.email} onChange={(value) => updateField('email', value)} />
          <TextInput label={l.availabilityUk} value={form.availabilityUk} onChange={(value) => updateField('availabilityUk', value)} />
          <TextInput label={l.availabilityEn} value={form.availabilityEn} onChange={(value) => updateField('availabilityEn', value)} />
          <TextInput label={l.sortOrder} type="number" value={form.sortOrder} onChange={(value) => updateField('sortOrder', value)} />
          <label className="flex min-h-11 items-center gap-3 pt-7 text-sm"><input type="checkbox" checked={form.active} onChange={(event) => updateField('active', event.target.checked)} className="h-5 w-5 rounded border-sky-100/20 bg-sky-950/70 accent-primary" />{l.active}</label>
        </div>
        {error && <ErrorState title={t('errorTitle')} description={error} />}
        <div className="flex flex-wrap gap-2">
          <button disabled={saving} className="primary-button">
            {saving ? <LoadingSpinner label={t('saving')} /> : editingId ? l.save : l.create}
          </button>
          {editingId && <button type="button" onClick={() => { setEditingId(null); setForm(emptyForm); }} className="secondary-button">{l.cancel}</button>}
        </div>
      </form>

      {loading ? <SkeletonCard variant="list" count={4} /> : (
        <div className="grid gap-3 md:grid-cols-2">
          {!contacts.length && <EmptyState icon={Building2} title={l.empty} description={t('emptyContactsDescription')} />}
          {contacts.map((contact) => (
            <article key={contact.id} className={`glass rounded-2xl p-4 ${contact.active ? '' : 'opacity-60'}`}>
              <div className="flex items-start justify-between gap-3">
                <div>
                  <p className="font-semibold">{localized(contact, 'name', language)}</p>
                  <p className="text-sm text-sky-100/70">{localized(contact, 'role', language)}</p>
                  {localized(contact, 'department', language) && <p className="text-xs text-sky-100/55">{localized(contact, 'department', language)}</p>}
                </div>
                <DataClassificationBadge level="Public" />
              </div>
              <div className="mt-3 space-y-1 text-sm text-sky-100/75">
                {contact.phone && <p>{l.phone}: {contact.phone}</p>}
                {contact.email && <p>{l.email}: {contact.email}</p>}
                {localized(contact, 'availability', language) && <p>{localized(contact, 'availability', language)}</p>}
                <p>{l.sortOrder}: {contact.sortOrder} · {contact.active ? t('active') : t('disabled')}</p>
              </div>
              <div className="mt-4 flex flex-wrap gap-2">
                <button onClick={() => edit(contact)} className="focus-ring rounded-xl border border-sky-100/20 px-3 py-2 text-sm">{l.edit}</button>
                {contact.active && <button disabled={busyId === contact.id} onClick={() => deactivate(contact.id)} className="focus-ring rounded-xl border border-rose-300/40 px-3 py-2 text-sm text-rose-100 disabled:opacity-60">{busyId === contact.id ? t('deactivating') : l.deactivate}</button>}
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

function TextInput({ label, value, onChange, type = 'text', placeholder = '', required = false }) {
  return <label className="block text-sm">{label}<input required={required} type={type} value={value} placeholder={placeholder} onChange={(e) => onChange(e.target.value)} className="field-control" /></label>;
}

function localized(item, field, language) {
  if (language === 'en') {
    return item[`${field}En`] || item[`${field}Uk`] || '';
  }
  return item[`${field}Uk`] || '';
}
