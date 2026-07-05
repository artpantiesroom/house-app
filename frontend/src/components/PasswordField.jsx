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
        className={`field-control pr-14 ${error ? 'field-error border-rose-300' : ''}`}
      />
      <button
        type="button"
        onClick={onToggle}
        aria-label={visible ? hideLabel : showLabel}
        className="focus-ring absolute right-2 top-1/2 grid h-10 w-10 -translate-y-1/2 place-items-center rounded-xl text-sky-100/75 transition hover:bg-sky-400/10 hover:text-sky-50"
      >
        {visible ? <EyeOff size={18} /> : <Eye size={18} />}
      </button>
    </div>
  );
}
