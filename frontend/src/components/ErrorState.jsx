import { AlertCircle } from 'lucide-react';

export default function ErrorState({ title, description, onRetry, retryLabel }) {
  return (
    <div className="rounded-3xl border border-rose-300/40 bg-rose-950/35 p-5 text-rose-50 shadow-glass sm:p-6">
      <div className="flex flex-col items-center gap-4 text-center sm:flex-row sm:items-start sm:justify-between sm:text-left">
        <div className="flex flex-col items-center gap-3 sm:flex-row sm:items-start">
          <div className="grid h-12 w-12 shrink-0 place-items-center rounded-2xl border border-rose-300/35 bg-rose-400/15 text-rose-100">
            <AlertCircle size={23} aria-hidden="true" />
          </div>
          <div>
            <h2 className="text-base font-semibold">{title}</h2>
            {description && <p className="mt-1 text-sm leading-6 text-rose-100/75">{description}</p>}
          </div>
        </div>
        {onRetry && (
          <button type="button" onClick={onRetry} className="danger-button">
            {retryLabel}
          </button>
        )}
      </div>
    </div>
  );
}
