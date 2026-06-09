export function getPasswordStrength(password) {
  let score = 0;
  if (password.length >= 8) score += 1;
  if (/[A-Z]/.test(password)) score += 1;
  if (/[a-z]/.test(password)) score += 1;
  if (/\d/.test(password)) score += 1;
  if (/[^A-Za-z0-9]/.test(password)) score += 1;
  return score;
}

export default function PasswordStrengthIndicator({ password }) {
  const score = getPasswordStrength(password);
  const label = score >= 5 ? 'сильний' : score >= 3 ? 'середній' : 'слабкий';
  const color = score >= 5 ? 'bg-emerald-300' : score >= 3 ? 'bg-amber-300' : 'bg-rose-300';

  return (
    <div className="space-y-2">
      <div className="h-2 overflow-hidden rounded-full bg-sky-950/70">
        <div className={`h-full rounded-full ${color}`} style={{ width: `${Math.max(score, 1) * 20}%` }} />
      </div>
      <p className="text-xs text-sky-100/70">Надійність пароля: {label}</p>
    </div>
  );
}
