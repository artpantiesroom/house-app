export function localeForLanguage(language) {
  return language === 'en' ? 'en-US' : 'uk-UA';
}

export function formatDate(value, language = 'uk', options) {
  if (!value) return '-';
  return new Date(value).toLocaleDateString(localeForLanguage(language), options);
}

export function formatDateTime(value, language = 'uk', options) {
  if (!value) return '-';
  return new Date(value).toLocaleString(localeForLanguage(language), options);
}
