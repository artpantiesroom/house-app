import { useEffect, useState } from 'react';
import DataClassificationBadge from '../../components/DataClassificationBadge.jsx';
import SkeletonCard from '../../components/SkeletonCard.jsx';
import { useData } from '../../context/DataContext.jsx';
import { useLanguage } from '../../context/LanguageContext.jsx';

export default function Home() {
  const data = useData();
  const { t } = useLanguage();
  const [ready, setReady] = useState(false);
  useEffect(() => { data.loadPageData(true, 700).then(setReady); }, []);
  if (!ready) return <div className="grid gap-4"><SkeletonCard /><SkeletonCard rows={4} /></div>;
  return (
    <section className="space-y-5">
      <div><h1 className="text-3xl font-bold">{t('residentHomeTitle')}</h1><p className="mt-2 text-sky-100/70">{data.buildingInfo.name} · {data.buildingInfo.address}</p></div>
      <div className="glass rounded-2xl p-5">
        <h2 className="text-xl font-semibold">Інформація про будинок</h2>
        <p className="mt-2 text-sky-100/70">Поверхів: {data.buildingInfo.floors} · тиха година {data.buildingInfo.quietHours}</p>
        <p className="mt-3 text-sm text-sky-100/65">{data.buildingInfo.policyNote}</p>
      </div>
      <div className="grid gap-3">
        <h2 className="text-xl font-semibold">Оголошення <DataClassificationBadge level="Public" /></h2>
        {data.announcements.map((announcement) => <article key={announcement.id} className="glass rounded-2xl p-4"><p className="font-semibold">{announcement.title}</p><p className="mt-2 text-sm text-sky-100/75">{announcement.body}</p><p className="mt-3 text-xs text-sky-100/55">{announcement.date}</p></article>)}
      </div>
    </section>
  );
}
