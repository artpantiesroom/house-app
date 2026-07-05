import { useEffect, useState } from 'react';
import { CreditCard } from 'lucide-react';
import DataClassificationBadge from '../../components/DataClassificationBadge.jsx';
import EmptyState from '../../components/EmptyState.jsx';
import ErrorState from '../../components/ErrorState.jsx';
import LoadingSpinner from '../../components/LoadingSpinner.jsx';
import SkeletonCard from '../../components/SkeletonCard.jsx';
import StatusBadge from '../../components/StatusBadge.jsx';
import { paymentsApi } from '../../api/paymentsApi.js';
import { residentsApi } from '../../api/residentsApi.js';
import { useLanguage } from '../../context/LanguageContext.jsx';
import { formatDate } from '../../utils/date.js';
import { formatMoney, minorToMoneyInput, parseMoneyToMinor } from '../../utils/money.js';
import { getPaymentStatusLabel, getPaymentTypeLabel, paymentStatuses, paymentTypes } from '../resident/MyPayments.jsx';

const emptyForm = {
  residentProfileId: '',
  amount: '',
  type: 'UTILITIES',
  status: 'PENDING',
  periodYear: new Date().getFullYear(),
  periodMonth: new Date().getMonth() + 1,
  titleUk: '',
  titleEn: '',
  descriptionUk: '',
  descriptionEn: '',
  dueDate: '',
};

export default function Payments() {
  const { language, t } = useLanguage();
  const [records, setRecords] = useState([]);
  const [residents, setResidents] = useState([]);
  const [filters, setFilters] = useState({ status: '', type: '', search: '', periodYear: '', periodMonth: '' });
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [busyId, setBusyId] = useState(null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const load = async (nextFilters = filters) => {
    setLoading(true);
    setError('');
    try {
      const [payments, residentItems] = await Promise.all([
        paymentsApi.listAdmin(nextFilters),
        residentsApi.list(),
      ]);
      setRecords(payments);
      setResidents(residentItems.filter((resident) => resident.enabled));
    } catch {
      setError(t('paymentsLoadFailed'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const submit = async (event) => {
    event.preventDefault();
    const amountMinor = parseMoneyToMinor(form.amount);
    if (!amountMinor || amountMinor <= 0) {
      setError(t('amountInvalid'));
      return;
    }
    setSaving(true);
    setError('');
    setSuccess('');
    try {
      const payload = {
        residentProfileId: Number(form.residentProfileId),
        type: form.type,
        status: form.status,
        amountMinor,
        currency: 'UAH',
        periodYear: Number(form.periodYear),
        periodMonth: Number(form.periodMonth),
        titleUk: form.titleUk.trim(),
        titleEn: form.titleEn.trim() || null,
        descriptionUk: form.descriptionUk.trim() || null,
        descriptionEn: form.descriptionEn.trim() || null,
        dueDate: form.dueDate,
      };
      if (editingId) {
        await paymentsApi.update(editingId, payload);
      } else {
        await paymentsApi.create(payload);
      }
      setForm(emptyForm);
      setEditingId(null);
      setSuccess(t('paymentSaved'));
      await load();
    } catch (err) {
      setError(err.message || t('paymentSaveFailed'));
    } finally {
      setSaving(false);
    }
  };

  const edit = (payment) => {
    setEditingId(payment.id);
    setForm({
      residentProfileId: payment.residentProfileId,
      amount: minorToMoneyInput(payment.amountMinor),
      type: payment.type,
      status: payment.status,
      periodYear: payment.periodYear,
      periodMonth: payment.periodMonth,
      titleUk: payment.titleUk || '',
      titleEn: payment.titleEn || '',
      descriptionUk: payment.descriptionUk || '',
      descriptionEn: payment.descriptionEn || '',
      dueDate: payment.dueDate || '',
    });
    setError('');
    setSuccess('');
  };

  const changeStatus = async (payment, status) => {
    setBusyId(payment.id);
    setError('');
    try {
      await paymentsApi.updateStatus(payment.id, status);
      await load();
    } catch (err) {
      setError(err.message || t('paymentSaveFailed'));
    } finally {
      setBusyId(null);
    }
  };

  const cancel = async (payment) => {
    setBusyId(payment.id);
    setError('');
    try {
      await paymentsApi.cancel(payment.id);
      await load();
    } catch (err) {
      setError(err.message || t('paymentSaveFailed'));
    } finally {
      setBusyId(null);
    }
  };

  return (
    <section className="space-y-5">
      <h1 className="text-3xl font-bold">{t('paymentsOverviewTitle')}</h1>

      <form onSubmit={submit} className="glass space-y-4 rounded-2xl p-4">
        <div className="grid gap-3 md:grid-cols-3">
          <label className="block text-sm">{t('resident')}<select required value={form.residentProfileId} onChange={(event) => setForm((current) => ({ ...current, residentProfileId: event.target.value }))} className="field-control"><option value=""></option>{residents.map((resident) => <option key={resident.id} value={resident.id}>{resident.name} · {resident.apartmentNumber || t('apartmentNotAssigned')}</option>)}</select></label>
          <TextInput label={t('amount')} value={form.amount} onChange={(value) => setForm((current) => ({ ...current, amount: value }))} required />
          <Select label={t('paymentType')} value={form.type} onChange={(value) => setForm((current) => ({ ...current, type: value }))} options={paymentTypes} t={t} />
          <Select label={t('status')} value={form.status} onChange={(value) => setForm((current) => ({ ...current, status: value }))} options={paymentStatuses} t={t} />
          <TextInput label={t('periodYear')} type="number" value={form.periodYear} onChange={(value) => setForm((current) => ({ ...current, periodYear: value }))} required />
          <TextInput label={t('periodMonth')} type="number" value={form.periodMonth} onChange={(value) => setForm((current) => ({ ...current, periodMonth: value }))} required />
          <TextInput label={t('titleUk')} value={form.titleUk} onChange={(value) => setForm((current) => ({ ...current, titleUk: value }))} required />
          <TextInput label={t('titleEn')} value={form.titleEn} onChange={(value) => setForm((current) => ({ ...current, titleEn: value }))} />
          <TextInput label={t('dueDate')} type="date" value={form.dueDate} onChange={(value) => setForm((current) => ({ ...current, dueDate: value }))} required />
          <TextArea label={t('descriptionUk')} value={form.descriptionUk} onChange={(value) => setForm((current) => ({ ...current, descriptionUk: value }))} />
          <TextArea label={t('descriptionEn')} value={form.descriptionEn} onChange={(value) => setForm((current) => ({ ...current, descriptionEn: value }))} />
        </div>
        {error && <ErrorState title={t('errorTitle')} description={error} />}
        {success && <p className="rounded-xl border border-emerald-300/40 bg-emerald-400/10 p-3 text-sm text-emerald-100">{success}</p>}
        <div className="flex flex-wrap gap-2">
          <button disabled={saving} className="primary-button">{saving ? <LoadingSpinner label={t('saving')} /> : editingId ? t('updatePayment') : t('createPayment')}</button>
          {editingId && <button type="button" onClick={() => { setEditingId(null); setForm(emptyForm); }} className="secondary-button">{t('cancel')}</button>}
        </div>
      </form>

      <div className="glass grid gap-3 rounded-2xl p-4 md:grid-cols-6">
        <FilterSelect label={t('status')} value={filters.status} onChange={(value) => setFilters((current) => ({ ...current, status: value }))} options={paymentStatuses} t={t} />
        <FilterSelect label={t('paymentType')} value={filters.type} onChange={(value) => setFilters((current) => ({ ...current, type: value }))} options={paymentTypes} t={t} />
        <TextInput label={t('search')} value={filters.search} onChange={(value) => setFilters((current) => ({ ...current, search: value }))} />
        <TextInput label={t('periodYear')} value={filters.periodYear} onChange={(value) => setFilters((current) => ({ ...current, periodYear: value }))} />
        <TextInput label={t('periodMonth')} value={filters.periodMonth} onChange={(value) => setFilters((current) => ({ ...current, periodMonth: value }))} />
        <div className="flex items-end"><button onClick={() => load(filters)} className="primary-button w-full text-sm">{t('applyFilters')}</button></div>
      </div>

      {loading ? <SkeletonCard variant="list" count={5} /> : (
        <div className="grid gap-3">
          {!records.length && <EmptyState icon={CreditCard} title={t('noPayments')} description={t('noPaymentsDescription')} />}
          {records.map((payment) => (
            <article key={payment.id} className="glass rounded-2xl p-4">
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div>
                  <p className="font-semibold">{payment.residentName} <DataClassificationBadge level="Internal" /></p>
                  <p className="text-sm text-sky-100/70">{payment.residentEmail} · {payment.apartmentNumber || t('apartmentNotAssigned')} · {t('sectionLabel')} {payment.buildingSection || '—'} · {t('floorLabel')} {payment.floor ?? '—'}</p>
                  <p className="mt-2 text-sm text-sky-100/75">{localized(payment, 'title', language)} · {t('period')}: {payment.periodMonth}/{payment.periodYear}</p>
                </div>
                <div className="text-right">
                  <p className="text-lg font-bold">{formatMoney(payment.amountMinor, payment.currency, language === 'en' ? 'en-US' : 'uk-UA')} <DataClassificationBadge level="Confidential" /></p>
                  <div className="mt-2 flex flex-wrap justify-end gap-2 text-xs">
                    <StatusBadge status={payment.status}>{getPaymentStatusLabel(payment.status, t)}</StatusBadge>
                    <StatusBadge>{getPaymentTypeLabel(payment.type, t)}</StatusBadge>
                  </div>
                </div>
              </div>
              <p className="mt-3 text-xs text-sky-100/55">{t('dueDate')}: {formatDate(payment.dueDate, language)}{payment.paidAt ? ` · ${t('paidAt')}: ${formatDate(payment.paidAt, language)}` : ''}</p>
              <div className="mt-4 flex flex-wrap gap-2">
                <button onClick={() => edit(payment)} className="focus-ring rounded-xl border border-sky-100/20 px-3 py-2 text-sm">{t('edit')}</button>
                {payment.status !== 'PAID' && <button disabled={busyId === payment.id} onClick={() => changeStatus(payment, 'PAID')} className="focus-ring rounded-xl border border-emerald-300/40 px-3 py-2 text-sm text-emerald-100 disabled:opacity-60">{t('markPaid')}</button>}
                {payment.status !== 'CANCELLED' && <button disabled={busyId === payment.id} onClick={() => cancel(payment)} className="focus-ring rounded-xl border border-rose-300/40 px-3 py-2 text-sm text-rose-100 disabled:opacity-60">{t('cancelPayment')}</button>}
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

function TextInput({ label, value, onChange, type = 'text', required = false }) {
  return <label className="block text-sm">{label}<input required={required} type={type} value={value} onChange={(event) => onChange(event.target.value)} className="field-control" /></label>;
}

function TextArea({ label, value, onChange }) {
  return <label className="block text-sm">{label}<textarea maxLength={1000} value={value} onChange={(event) => onChange(event.target.value)} className="field-control min-h-24" /></label>;
}

function Select({ label, value, onChange, options, t }) {
  return <label className="block text-sm">{label}<select value={value} onChange={(event) => onChange(event.target.value)} className="field-control">{options.map(([optionValue, labelKey]) => <option key={optionValue} value={optionValue}>{t(labelKey)}</option>)}</select></label>;
}

function FilterSelect({ label, value, onChange, options, t }) {
  return <label className="block text-sm">{label}<select value={value} onChange={(event) => onChange(event.target.value)} className="field-control"><option value="">{t('all')}</option>{options.map(([optionValue, labelKey]) => <option key={optionValue} value={optionValue}>{t(labelKey)}</option>)}</select></label>;
}

function localized(item, field, language) {
  if (language === 'en') {
    return item[`${field}En`] || item[`${field}Uk`] || '';
  }
  return item[`${field}Uk`] || '';
}
