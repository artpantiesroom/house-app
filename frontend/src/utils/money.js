export function formatMoney(amountMinor, currency = 'UAH', locale = 'uk-UA') {
  const minor = Number.isFinite(Number(amountMinor)) ? Number(amountMinor) : 0;
  return new Intl.NumberFormat(locale, {
    style: 'currency',
    currency,
    minimumFractionDigits: 2,
  }).format(minor / 100);
}

export function parseMoneyToMinor(value) {
  const normalized = String(value || '').replace(/\s/g, '').replace(',', '.');
  if (!/^\d+(\.\d{0,2})?$/.test(normalized)) {
    return null;
  }
  const [majorPart, minorPart = ''] = normalized.split('.');
  const major = Number.parseInt(majorPart || '0', 10);
  const paddedMinor = `${minorPart}00`.slice(0, 2);
  const minor = Number.parseInt(paddedMinor, 10);
  return (major * 100) + minor;
}

export function minorToMoneyInput(amountMinor) {
  const minor = Number.isFinite(Number(amountMinor)) ? Number(amountMinor) : 0;
  const major = Math.trunc(minor / 100);
  const cents = String(Math.abs(minor % 100)).padStart(2, '0');
  return `${major}.${cents}`;
}
