export const UKRAINIAN_PHONE_PLACEHOLDER = '+38(___)-___-__-__';

const PHONE_PATTERN = /^\+38\(\d{3}\)-\d{3}-\d{2}-\d{2}$/;

export function formatUkrainianPhone(value) {
  const digits = String(value || '').replace(/\D/g, '');
  const localDigits = digits.startsWith('38') ? digits.slice(2, 12) : digits.slice(0, 10);

  if (!localDigits) return '';

  let formatted = '+38(';
  formatted += localDigits.slice(0, 3);
  if (localDigits.length >= 3) formatted += ')';
  if (localDigits.length > 3) formatted += `-${localDigits.slice(3, 6)}`;
  if (localDigits.length > 6) formatted += `-${localDigits.slice(6, 8)}`;
  if (localDigits.length > 8) formatted += `-${localDigits.slice(8, 10)}`;
  return formatted;
}

export function isValidUkrainianPhone(value) {
  return !value || PHONE_PATTERN.test(value);
}
