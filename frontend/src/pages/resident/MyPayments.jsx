import { useEffect, useState } from 'react';
import DataClassificationBadge from '../../components/DataClassificationBadge.jsx';
import SkeletonCard from '../../components/SkeletonCard.jsx';
import StatusBadge from '../../components/StatusBadge.jsx';
import { useAuth } from '../../context/AuthContext.jsx';
import { useData } from '../../context/DataContext.jsx';
import { useLanguage } from '../../context/LanguageContext.jsx';

export default function MyPayments() {
  const data = useData();
  const { user } = useAuth();
  const { t } = useLanguage();
  const [records, setRecords] = useState([]);
  const [ready, setReady] = useState(false);
  const resident = data.residents.find((item) => item.id === user.residentId);
  useEffect(() => { data.loadPayments(data.payments.filter((payment) => payment.residentId === user.residentId)).then((items) => { setRecords(items); setReady(true); }); }, [data.payments, user.residentId]);
  if (!ready) return <SkeletonCard rows={4} />;
  return (
    <section className="space-y-5">
      <h1 className="text-3xl font-bold">{t('myPaymentsTitle')}</h1>
      <div className="glass rounded-2xl p-4 text-sm text-sky-100/75">Квартира {resident?.apartment} <DataClassificationBadge level="Confidential" /></div>
      <div className="grid gap-3">
        {records.map((payment) => <article key={payment.id} className="glass rounded-2xl p-4"><div className="flex items-center justify-between gap-3"><div><p className="font-semibold">{payment.month}</p><p className="text-sm text-sky-100/70">До сплати: {payment.dueDate}</p></div><div className="text-right"><p className="font-bold">${payment.amount.toLocaleString()} <DataClassificationBadge level="Confidential" /></p><StatusBadge status={payment.status} /></div></div></article>)}
      </div>
    </section>
  );
}
