import { AlertCircle } from 'lucide-react';

export default function ErrorState({ title, description, onRetry, retryLabel }) {
  return (
    <div className="rounded-2xl border border-rose-300/40 bg-rose-950/35 p-5 text-rose-50 shadow-glass">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div className="flex gap-3">
          <div className="grid h-11 w-11 shrink-0 place-items-center rounded-xl border border-rose-300/35 bg-rose-400/15 text-rose-100">
            <AlertCircle size={22} aria-hidden="true" />
          </div>
          <div>
            <h2 className="text-base font-semibold">{title}</h2>
            {description && <p className="mt-1 text-sm leading-6 text-rose-100/75">{description}</p>}
          </div>
        </div>
        {onRetry && (
          <button type="button" onClick={onRetry} className="focus-ring rounded-xl border border-rose-200/40 px-4 py-2 text-sm font-semibold text-rose-50 hover:bg-rose-400/15">
            {retryLabel}
          </button>
        )}
      </div>
    </div>
  );
}
