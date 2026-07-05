export default function SkeletonCard({ rows = 3, variant = 'card', count = 1 }) {
  if (variant === 'kpi') {
    return (
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {Array.from({ length: count }).map((_, index) => (
          <div key={index} className="glass animate-pulse rounded-3xl p-5">
            <div className="mb-5 h-10 w-10 rounded-xl bg-sky-200/20" />
            <div className="mb-3 h-3 w-2/3 rounded bg-sky-200/15" />
            <div className="h-8 w-20 rounded bg-sky-200/20" />
          </div>
        ))}
      </div>
    );
  }

  if (variant === 'list') {
    return (
      <div className="grid gap-3">
        {Array.from({ length: count }).map((_, index) => (
          <div key={index} className="glass animate-pulse rounded-3xl p-5">
            <div className="flex items-start justify-between gap-4">
              <div className="min-w-0 flex-1">
                <div className="mb-3 h-5 w-2/3 rounded bg-sky-200/20" />
                <div className="mb-2 h-3 w-full rounded bg-sky-200/10" />
                <div className="h-3 w-1/2 rounded bg-sky-200/10" />
              </div>
              <div className="h-7 w-24 rounded-full bg-sky-200/15" />
            </div>
          </div>
        ))}
      </div>
    );
  }

  if (variant === 'form') {
    return (
      <div className="glass animate-pulse rounded-3xl p-5">
        <div className="grid gap-3 md:grid-cols-3">
          {Array.from({ length: rows }).map((_, index) => (
            <div key={index}>
              <div className="mb-2 h-3 w-24 rounded bg-sky-200/15" />
              <div className="h-11 rounded-xl bg-sky-200/10" />
            </div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="glass animate-pulse rounded-3xl p-5">
      <div className="mb-4 h-5 w-2/3 rounded bg-sky-200/20" />
      {Array.from({ length: rows }).map((_, index) => (
        <div key={index} className="mb-3 h-3 rounded bg-sky-200/10" />
      ))}
    </div>
  );
}
