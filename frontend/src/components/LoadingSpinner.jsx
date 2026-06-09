export default function LoadingSpinner({ label = 'Завантаження' }) {
  return (
    <span className="inline-flex items-center gap-2 text-sm text-sky-100">
      <span className="h-4 w-4 animate-spin rounded-full border-2 border-sky-100/40 border-t-accent" />
      {label}
    </span>
  );
}
