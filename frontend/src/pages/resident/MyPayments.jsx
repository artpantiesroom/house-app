import { useEffect, useState } from 'react';
import { CreditCard } from 'lucide-react';
import DataClassificationBadge from '../../components/DataClassificationBadge.jsx';
import EmptyState from '../../components/EmptyState.jsx';
import ErrorState from '../../components/ErrorState.jsx';
import PageHeader from '../../components/PageHeader.jsx';
import SkeletonCard from '../../components/SkeletonCard.jsx';
import StatusBadge from '../../components/StatusBadge.jsx';
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

  if (loading) return <SkeletonCard variant="list" count={3} />;

  return (
    <section className="space-y-5">
      <PageHeader title={t('myPaymentsTitle')} subtitle={t('myPaymentsSubtitle')} />
      <div className="glass grid gap-3 rounded-2xl p-4 md:grid-cols-5">
        <FilterSelect label={t('status')} value={filters.status} onChange={(value) => setFilters((current) => ({ ...current, status: value }))} options={paymentStatuses} t={t} />
        <FilterSelect label={t('paymentType')} value={filters.type} onChange={(value) => setFilters((current) => ({ ...current, type: value }))} options={paymentTypes} t={t} />
        <TextInput label={t('periodYear')} value={filters.periodYear} onChange={(value) => setFilters((current) => ({ ...current, periodYear: value }))} />
        <TextInput label={t('periodMonth')} value={filters.periodMonth} onChange={(value) => setFilters((current) => ({ ...current, periodMonth: value }))} />
        <div className="flex items-end"><button onClick={() => load(filters)} className="primary-button w-full text-sm">{t('applyFilters')}</button></div>
      </div>
      {error && <ErrorState title={t('errorTitle')} description={error} onRetry={() => load(filters)} retryLabel={t('retry')} />}
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
                  <StatusBadge status={payment.status}>{getPaymentStatusLabel(payment.status, t)}</StatusBadge>
                  <StatusBadge>{getPaymentTypeLabel(payment.type, t)}</StatusBadge>
                </div>
              </div>
            </div>
            <p className="mt-3 text-xs text-sky-100/55">{t('dueDate')}: {formatDate(payment.dueDate, language)}{payment.paidAt ? ` · ${t('paidAt')}: ${formatDate(payment.paidAt, language)}` : ''}</p>
          </article>
        )) : <EmptyState icon={CreditCard} title={t('noPayments')} description={t('noPaymentsDescription')} />}
      </div>
    </section>
  );
}

function FilterSelect({ label, value, onChange, options, t }) {
  return <label className="block text-sm">{label}<select value={value} onChange={(event) => onChange(event.target.value)} className="field-control"><option value="">{t('all')}</option>{options.map(([optionValue, labelKey]) => <option key={optionValue} value={optionValue}>{t(labelKey)}</option>)}</select></label>;
}

function TextInput({ label, value, onChange }) {
  return <label className="block text-sm">{label}<input value={value} onChange={(event) => onChange(event.target.value)} className="field-control" /></label>;
}

function localized(item, field, language) {
  if (language === 'en') {
    return item[`${field}En`] || item[`${field}Uk`] || '';
  }
  return item[`${field}Uk`] || '';
}
