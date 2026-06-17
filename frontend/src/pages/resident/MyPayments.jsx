import { useEffect, useState } from 'react';
import DataClassificationBadge from '../../components/DataClassificationBadge.jsx';
import SkeletonCard from '../../components/SkeletonCard.jsx';
import { paymentsApi } from '../../api/paymentsApi.js';
import { useLanguage } from '../../context/LanguageContext.jsx';
import { formatDate } from '../../utils/date.js';
import { formatMoney } from '../../utils/money.js';

export const paymentTypes = [
  ['RENT', 'paymentTypeRent'],
  ['UTILITIES', 'paymentTypeUtilities'],
  ['MAINTENANCE', 'paymentTypeMaintenance'],
  ['SECURITY', 'paymentTypeSecurity'],
  ['PARKING', 'paymentTypeParking'],
  ['OTHER', 'paymentTypeOther'],
];

export const paymentStatuses = [
  ['PENDING', 'paymentStatusPending'],
  ['PAID', 'paymentStatusPaid'],
  ['OVERDUE', 'paymentStatusOverdue'],
  ['CANCELLED', 'paymentStatusCancelled'],
];

export function getPaymentTypeLabel(type, t) {
  const match = paymentTypes.find(([value]) => value === type);
  return match ? t(match[1]) : type || t('paymentTypeOther');
}

export function getPaymentStatusLabel(status, t) {
  const match = paymentStatuses.find(([value]) => value === status);
  return match ? t(match[1]) : status || t('paymentStatusPending');
}

export default function MyPayments() {
  const { language, t } = useLanguage();
  const [records, setRecords] = useState([]);
  const [filters, setFilters] = useState({ status: '', type: '', periodYear: '', periodMonth: '' });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = async (nextFilters = filters) => {
    setLoading(true);
    setError('');
    try {
      setRecords(await paymentsApi.listResident(nextFilters));
    } catch {
      setError(t('paymentsLoadFailed'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  if (loading) return <SkeletonCard rows={5} />;

  return (
    <section className="space-y-5">
      <h1 className="text-3xl font-bold">{t('myPaymentsTitle')}</h1>
      <div className="glass grid gap-3 rounded-2xl p-4 md:grid-cols-5">
        <FilterSelect label={t('status')} value={filters.status} onChange={(value) => setFilters((current) => ({ ...current, status: value }))} options={paymentStatuses} t={t} />
        <FilterSelect label={t('paymentType')} value={filters.type} onChange={(value) => setFilters((current) => ({ ...current, type: value }))} options={paymentTypes} t={t} />
        <TextInput label={t('periodYear')} value={filters.periodYear} onChange={(value) => setFilters((current) => ({ ...current, periodYear: value }))} />
        <TextInput label={t('periodMonth')} value={filters.periodMonth} onChange={(value) => setFilters((current) => ({ ...current, periodMonth: value }))} />
        <div className="flex items-end"><button onClick={() => load(filters)} className="focus-ring h-10 rounded-xl bg-primary px-4 text-sm font-semibold">{t('applyFilters')}</button></div>
      </div>
      {error && <p className="rounded-xl border border-rose-300/40 bg-rose-950/40 p-3 text-sm text-rose-100">{error}</p>}
      <div className="grid gap-3">
        {records.length ? records.map((payment) => (
          <article key={payment.id} className="glass rounded-2xl p-4">
            <div className="flex flex-wrap items-start justify-between gap-3">
              <div>
                <p className="font-semibold">{localized(payment, 'title', language)}</p>
                <p className="text-sm text-sky-100/70">{t('period')}: {payment.periodMonth}/{payment.periodYear} · {payment.apartmentNumber || t('apartmentNotAssigned')}</p>
                {localized(payment, 'description', language) && <p className="mt-2 text-sm text-sky-100/75">{localized(payment, 'description', language)}</p>}
              </div>
              <div className="text-right">
                <p className="text-lg font-bold">{formatMoney(payment.amountMinor, payment.currency, language === 'en' ? 'en-US' : 'uk-UA')} <DataClassificationBadge level="Confidential" /></p>
                <div className="mt-2 flex flex-wrap justify-end gap-2 text-xs">
                  <Badge>{getPaymentStatusLabel(payment.status, t)}</Badge>
                  <Badge>{getPaymentTypeLabel(payment.type, t)}</Badge>
                </div>
              </div>
            </div>
            <p className="mt-3 text-xs text-sky-100/55">{t('dueDate')}: {formatDate(payment.dueDate, language)}{payment.paidAt ? ` · ${t('paidAt')}: ${formatDate(payment.paidAt, language)}` : ''}</p>
          </article>
        )) : <div className="glass rounded-2xl p-5 text-sky-100/70">{t('noPayments')}</div>}
      </div>
    </section>
  );
}

function FilterSelect({ label, value, onChange, options, t }) {
  return <label className="block text-sm">{label}<select value={value} onChange={(event) => onChange(event.target.value)} className="focus-ring mt-1 h-10 w-full rounded-xl border border-sky-100/15 bg-sky-950/80 px-3"><option value="">{t('all')}</option>{options.map(([optionValue, labelKey]) => <option key={optionValue} value={optionValue}>{t(labelKey)}</option>)}</select></label>;
}

function TextInput({ label, value, onChange }) {
  return <label className="block text-sm">{label}<input value={value} onChange={(event) => onChange(event.target.value)} className="focus-ring mt-1 h-10 w-full rounded-xl border border-sky-100/15 bg-sky-950/50 px-3" /></label>;
}

function Badge({ children }) {
  return <span className="rounded-full border border-sky-100/15 bg-sky-950/50 px-2 py-1">{children}</span>;
}

function localized(item, field, language) {
  if (language === 'en') {
    return item[`${field}En`] || item[`${field}Uk`] || '';
  }
  return item[`${field}Uk`] || '';
}
