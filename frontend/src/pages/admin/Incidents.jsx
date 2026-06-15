import { useEffect, useState } from 'react';
import SkeletonCard from '../../components/SkeletonCard.jsx';
import StatusBadge from '../../components/StatusBadge.jsx';
import { useData } from '../../context/DataContext.jsx';
import { useLanguage } from '../../context/LanguageContext.jsx';

export default function Incidents() {
  const data = useData();
  const { t } = useLanguage();
  const [ready, setReady] = useState(false);
  useEffect(() => { data.loadPageData(true, 850).then(setReady); }, []);
  if (!ready) return <SkeletonCard rows={5} />;
  return (
    <section className="space-y-5">
      <div><h1 className="text-3xl font-bold">{t('securityIncidentsTitle')}</h1><p className="mt-2 text-sky-100/70">Записи реагування на інциденти симулюються для прототипу моніторингу.</p></div>
      <div className="grid gap-4 md:grid-cols-3">
        {data.incidents.map((incident) => (
          <article key={incident.id} className="glass rounded-2xl p-4">
            <div className="mb-3 flex items-center justify-between gap-2"><StatusBadge status={incident.severity} /><StatusBadge status={incident.status} /></div>
            <h2 className="text-lg font-semibold">{incident.title}</h2>
            <p className="mt-2 text-sm text-sky-100/70">{new Date(incident.timestamp).toLocaleString()}</p>
            <p className="mt-3 text-sm text-sky-100/80">{incident.recommendedAction}</p>
          </article>
        ))}
      </div>
    </section>
  );
}
