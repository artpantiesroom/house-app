import { useEffect, useState } from 'react';
import DataClassificationBadge from '../../components/DataClassificationBadge.jsx';
import SkeletonCard from '../../components/SkeletonCard.jsx';
import StatusBadge from '../../components/StatusBadge.jsx';
import { useData } from '../../context/DataContext.jsx';

export default function Payments() {
  const data = useData();
  const [records, setRecords] = useState([]);
  const [ready, setReady] = useState(false);
  useEffect(() => { data.loadPayments(data.payments).then((items) => { setRecords(items); setReady(true); }); }, [data.payments]);
  if (!ready) return <SkeletonCard rows={6} />;
  return (
    <section className="space-y-5">
      <h1 className="text-3xl font-bold">Огляд платежів</h1>
      <div className="grid gap-3">
        {records.map((payment) => {
          const resident = data.residents.find((item) => item.id === payment.residentId);
          return (
            <article key={payment.id} className="glass rounded-2xl p-4">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div><p className="font-semibold">{resident?.name} <DataClassificationBadge level="Internal" /></p><p className="text-sm text-sky-100/70">Кв. {resident?.apartment} <DataClassificationBadge level="Confidential" /> · {payment.month}</p></div>
                <div className="text-right"><p className="text-lg font-bold">${payment.amount.toLocaleString()} <DataClassificationBadge level="Confidential" /></p><StatusBadge status={payment.status} /></div>
              </div>
            </article>
          );
        })}
      </div>
    </section>
  );
}
