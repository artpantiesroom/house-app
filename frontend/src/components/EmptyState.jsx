import { Inbox } from 'lucide-react';

export default function EmptyState({ icon: Icon = Inbox, title, description, action }) {
  return (
    <div className="glass rounded-3xl px-5 py-8 text-center sm:px-8">
      <div className="mx-auto mb-5 grid h-14 w-14 place-items-center rounded-3xl border border-sky-100/15 bg-sky-400/10 text-primary shadow-glass">
        <Icon size={26} aria-hidden="true" />
      </div>
      <h2 className="text-lg font-semibold leading-tight text-sky-50">{title}</h2>
      {description && <p className="mx-auto mt-3 max-w-md text-sm leading-6 text-sky-100/68">{description}</p>}
      {action && <div className="mt-5 flex justify-center">{action}</div>}
    </div>
  );
}
