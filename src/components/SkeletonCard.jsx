export default function SkeletonCard({ rows = 3 }) {
  return (
    <div className="glass animate-pulse rounded-2xl p-4">
      <div className="mb-4 h-5 w-2/3 rounded bg-sky-200/20" />
      {Array.from({ length: rows }).map((_, index) => (
        <div key={index} className="mb-3 h-3 rounded bg-sky-200/10" />
      ))}
    </div>
  );
}
