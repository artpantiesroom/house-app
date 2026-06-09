import DataClassificationBadge from '../../components/DataClassificationBadge.jsx';
import { useData } from '../../context/DataContext.jsx';

export default function Contacts() {
  const { contacts, buildingInfo } = useData();
  return (
    <section className="space-y-5">
      <h1 className="text-3xl font-bold">Контакти</h1>
      <div className="glass rounded-2xl p-5">
        <h2 className="text-xl font-semibold">{buildingInfo.name}</h2>
        <p className="mt-2 text-sky-100/70">{buildingInfo.address}</p>
        <p className="mt-2 text-sm text-sky-100/65">Тиха година {buildingInfo.quietHours}</p>
      </div>
      <div className="grid gap-3">
        {contacts.map((contact) => <article key={contact.id} className="glass rounded-2xl p-4"><div className="flex items-center justify-between gap-3"><div><p className="font-semibold">{contact.label}</p><p className="text-sm text-sky-100/70">{contact.value}</p></div><DataClassificationBadge level={contact.classification} /></div></article>)}
      </div>
    </section>
  );
}
