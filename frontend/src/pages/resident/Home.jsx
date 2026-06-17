import { useEffect, useState } from 'react';
import DataClassificationBadge from '../../components/DataClassificationBadge.jsx';
import SkeletonCard from '../../components/SkeletonCard.jsx';
import { announcementsApi } from '../../api/announcementsApi.js';
import { buildingInfo } from '../../config/buildingInfo.js';
import { useLanguage } from '../../context/LanguageContext.jsx';
import { formatDateTime } from '../../utils/date.js';

const labels = {
  uk: {
    buildingInfo: 'Інформація про будинок',
    floors: 'Поверхів',
    quietHours: 'тиха година',
    announcements: 'Оголошення',
    loadError: 'Не вдалося завантажити оголошення.',
    empty: 'Актуальних оголошень немає.',
    publishedAt: 'Опубліковано',
  },
  en: {
    buildingInfo: 'Building information',
    floors: 'Floors',
    quietHours: 'quiet hours',
    announcements: 'Announcements',
    loadError: 'Could not load announcements.',
    empty: 'No current announcements.',
    publishedAt: 'Published',
  },
};

const categoryLabels = {
  GENERAL: { uk: 'Загальне', en: 'General' },
  MAINTENANCE: { uk: 'Обслуговування', en: 'Maintenance' },
  PAYMENT: { uk: 'Оплати', en: 'Payments' },
  SECURITY: { uk: 'Безпека', en: 'Security' },
  EVENT: { uk: 'Подія', en: 'Event' },
  OTHER: { uk: 'Інше', en: 'Other' },
};

const priorityLabels = {
  LOW: { uk: 'Низький', en: 'Low' },
  NORMAL: { uk: 'Звичайний', en: 'Normal' },
  HIGH: { uk: 'Високий', en: 'High' },
  URGENT: { uk: 'Терміновий', en: 'Urgent' },
};

export default function Home() {
  const { language, t } = useLanguage();
  const l = labels[language];
  const [announcements, setAnnouncements] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let mounted = true;
    announcementsApi.listResident()
      .then((items) => {
        if (mounted) setAnnouncements(items);
      })
      .catch(() => {
        if (mounted) setError(l.loadError);
      })
      .finally(() => {
        if (mounted) setLoading(false);
      });
    return () => {
      mounted = false;
    };
  }, [l.loadError]);

  if (loading) return <div className="grid gap-4"><SkeletonCard /><SkeletonCard rows={4} /></div>;

  return (
    <section className="space-y-5">
      <div><h1 className="text-3xl font-bold">{t('residentHomeTitle')}</h1><p className="mt-2 text-sky-100/70">{buildingInfo.name} · {buildingInfo.address}</p></div>
      <div className="glass rounded-2xl p-5">
        <h2 className="text-xl font-semibold">{l.buildingInfo}</h2>
        <p className="mt-2 text-sky-100/70">{l.floors}: {buildingInfo.floors} · {l.quietHours} {buildingInfo.quietHours}</p>
        <p className="mt-3 text-sm text-sky-100/65">{buildingInfo.policyNote[language]}</p>
      </div>
      <div className="grid gap-3">
        <h2 className="text-xl font-semibold">{l.announcements} <DataClassificationBadge level="Public" /></h2>
        {error && <p className="glass rounded-2xl border border-rose-300/40 p-4 text-rose-100">{error}</p>}
        {!error && !announcements.length && <p className="glass rounded-2xl p-4 text-sky-100/70">{l.empty}</p>}
        {announcements.map((announcement) => (
          <article key={announcement.id} className="glass rounded-2xl p-4">
            <p className="font-semibold">{localized(announcement, 'title', language)}</p>
            <p className="mt-2 text-sm text-sky-100/75">{localized(announcement, 'body', language)}</p>
            <div className="mt-3 flex flex-wrap gap-2 text-xs">
              <Pill>{categoryLabels[announcement.category]?.[language] || announcement.category}</Pill>
              <Pill>{priorityLabels[announcement.priority]?.[language] || announcement.priority}</Pill>
            </div>
            <p className="mt-3 text-xs text-sky-100/55">{l.publishedAt}: {formatDateTime(announcement.publishedAt, language)}</p>
          </article>
        ))}
      </div>
    </section>
  );
}

function localized(item, field, language) {
  if (language === 'en') {
    return item[`${field}En`] || item[`${field}Uk`] || '';
  }
  return item[`${field}Uk`] || '';
}

function Pill({ children }) {
  return <span className="rounded-full border border-sky-100/15 bg-sky-950/40 px-2 py-1">{children}</span>;
}
