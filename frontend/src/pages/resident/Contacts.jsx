import { useEffect, useState } from 'react';
import { Building2 } from 'lucide-react';
import DataClassificationBadge from '../../components/DataClassificationBadge.jsx';
import EmptyState from '../../components/EmptyState.jsx';
import ErrorState from '../../components/ErrorState.jsx';
import PageHeader from '../../components/PageHeader.jsx';
import SkeletonCard from '../../components/SkeletonCard.jsx';
import { contactsApi } from '../../api/contactsApi.js';
import { buildingInfo } from '../../config/buildingInfo.js';
import { useLanguage } from '../../context/LanguageContext.jsx';

const labels = {
  uk: {
    loadError: 'Не вдалося завантажити контакти.',
    empty: 'Активних контактів немає.',
    phone: 'Телефон',
    email: 'Email',
    quietHours: 'Тиха година',
  },
  en: {
    loadError: 'Could not load contacts.',
    empty: 'No active contacts.',
    phone: 'Phone',
    email: 'Email',
    quietHours: 'Quiet hours',
  },
};

export default function Contacts() {
  const { language, t } = useLanguage();
  const l = labels[language];
  const [contacts, setContacts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = () => {
    let mounted = true;
    setLoading(true);
    setError('');
    contactsApi.listResident()
      .then((items) => {
        if (mounted) setContacts(items);
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
  };

  useEffect(() => {
    return load();
  }, [l.loadError]);

  if (loading) return <SkeletonCard variant="list" count={3} />;

  return (
    <section className="space-y-5">
      <PageHeader title={t('contactsTitle')} subtitle={t('contactsSubtitle')} />
      <div className="glass rounded-2xl p-5">
        <h2 className="text-xl font-semibold">{buildingInfo.name}</h2>
        <p className="mt-2 text-sky-100/70">{buildingInfo.address}</p>
        <p className="mt-2 text-sm text-sky-100/65">{l.quietHours} {buildingInfo.quietHours}</p>
      </div>
      <div className="grid gap-3">
        {error && <ErrorState title={t('errorTitle')} description={error} onRetry={load} retryLabel={t('retry')} />}
        {!error && !contacts.length && <EmptyState icon={Building2} title={l.empty} description={t('emptyContactsDescription')} />}
        {contacts.map((contact) => (
          <article key={contact.id} className="glass rounded-2xl p-4">
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
            </div>
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
