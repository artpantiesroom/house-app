import { Inbox } from 'lucide-react';

export default function EmptyState({ icon: Icon = Inbox, title, description, action }) {
  return (
    <div className="glass rounded-2xl p-6 text-center">
      <div className="mx-auto mb-4 grid h-12 w-12 place-items-center rounded-2xl border border-sky-100/15 bg-sky-400/10 text-primary">
        <Icon size={24} aria-hidden="true" />
      </div>
      <h2 className="text-lg font-semibold text-sky-50">{title}</h2>
      {description && <p className="mx-auto mt-2 max-w-md text-sm leading-6 text-sky-100/70">{description}</p>}
      {action && <div className="mt-4 flex justify-center">{action}</div>}
    </div>
  );
}
