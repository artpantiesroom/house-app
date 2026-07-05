import { Eye, EyeOff } from 'lucide-react';

export default function PasswordField({
  id,
  value,
  onChange,
  visible,
  onToggle,
  showLabel,
  hideLabel,
  error,
  autoComplete,
}) {
  return (
    <div className="relative mt-1">
      <input
        id={id}
        type={visible ? 'text' : 'password'}
        value={value}
        onChange={onChange}
        autoComplete={autoComplete}
        className={`focus-ring w-full rounded-xl border bg-sky-950/50 px-4 py-3 pr-14 text-sky-50 shadow-sm hover:border-sky-200/35 ${error ? 'field-error border-rose-300' : 'border-sky-100/15'}`}
      />
      <button
        type="button"
        onClick={onToggle}
        aria-label={visible ? hideLabel : showLabel}
        className="focus-ring absolute right-2 top-1/2 grid h-10 w-10 -translate-y-1/2 place-items-center rounded-lg text-sky-100/75 transition hover:bg-sky-400/10 hover:text-sky-50"
      >
        {visible ? <EyeOff size={18} /> : <Eye size={18} />}
      </button>
    </div>
  );
}
