export default function PageHeader({ title, subtitle, action }) {
  return (
    <header className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
      <div className="max-w-3xl">
        <p className="mb-2 text-xs font-semibold uppercase tracking-[0.18em] text-primaryLight/70">Genesis</p>
        <h1 className="text-2xl font-bold leading-tight text-sky-50 sm:text-3xl">{title}</h1>
        {subtitle && <p className="mt-2 text-sm leading-6 text-sky-100/68 sm:text-base">{subtitle}</p>}
      </div>
      {action && <div className="flex shrink-0 flex-wrap gap-2">{action}</div>}
    </header>
  );
}
