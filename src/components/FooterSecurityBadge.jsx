import { Lock } from 'lucide-react';

export default function FooterSecurityBadge() {
  return (
    <div className="mt-6 flex items-center justify-center gap-2 text-xs text-sky-100/75">
      <Lock size={14} />
      <span>З'єднання захищено · TLS 1.3 (симуляція)</span>
    </div>
  );
}
