import { useState } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Building2, ShieldCheck } from 'lucide-react';
import LoadingSpinner from '../components/LoadingSpinner.jsx';
import LanguageToggle from '../components/LanguageToggle.jsx';
import PasswordField from '../components/PasswordField.jsx';
import PasswordStrengthIndicator, { getPasswordStrength } from '../components/PasswordStrengthIndicator.jsx';
import FooterSecurityBadge from '../components/FooterSecurityBadge.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { useLanguage } from '../context/LanguageContext.jsx';

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const normalizeEmail = (value) => String(value || '').replace(/[<>"]/g, '').trim();

export default function Login() {
  const { user, authReady, login } = useAuth();
  const { t } = useLanguage();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ email: '', password: '', remember: false });
  const [errors, setErrors] = useState({});
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [passwordVisible, setPasswordVisible] = useState(false);

  if (!authReady) return <main className="grid min-h-screen place-items-center"><LoadingSpinner label={t('restoringSession')} /></main>;
  if (user?.mustChangePassword) return <Navigate to="/change-password" replace />;
  if (user?.role === 'ADMIN') return <Navigate to="/admin/dashboard" replace />;
  if (user?.role === 'RESIDENT') return <Navigate to="/resident/home" replace />;

  const validate = () => {
    const next = {};
    if (!emailPattern.test(form.email)) next.email = t('validEmailRequired');
    if (getPasswordStrength(form.password) < 5) next.password = t('strongPasswordRequired');
    setErrors(next);
    return Object.keys(next).length === 0;
  };

  const updateField = (field, value) => {
    setForm({ ...form, [field]: field === 'email' ? normalizeEmail(value) : value });
    setErrors((current) => ({ ...current, [field]: '', form: '' }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (loading) return;
    setMessage('');
    if (!validate()) return;
    setLoading(true);
    setErrors({});
    try {
      const loggedInUser = await login({ email: normalizeEmail(form.email), password: form.password, remember: form.remember });
      const fallback = loggedInUser.mustChangePassword ? '/change-password' : loggedInUser.role === 'ADMIN' ? '/admin/dashboard' : '/resident/home';
      navigate(loggedInUser.mustChangePassword ? fallback : location.state?.from?.pathname || fallback, { replace: true });
    } catch (error) {
      setErrors({ form: error.message });
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="grid min-h-screen place-items-center px-4 py-8">
      <motion.section initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} className="w-full max-w-md">
        <div className="mb-8 flex justify-center">
          <LanguageToggle compact />
        </div>
        <div className="mb-8 text-center">
          <div className="mx-auto mb-5 grid h-20 w-20 place-items-center rounded-[1.75rem] border border-sky-100/15 bg-primary text-white shadow-glass">
            <Building2 size={36} aria-hidden="true" />
          </div>
          <h1 className="text-4xl font-bold leading-tight text-sky-50">Genesis</h1>
          <p className="mt-2 text-base font-semibold text-primaryLight">{t('loginSubtitle')}</p>
          <p className="mx-auto mt-3 max-w-xs text-sm leading-6 text-sky-100/62">{t('loginFormSubtitle')}</p>
        </div>
        <div className="glass rounded-[2rem] p-5 shadow-glass sm:p-6">
          <div className="mb-6 border-b border-sky-100/10 pb-5 text-center">
            <h2 className="text-xl font-semibold text-sky-50">{t('loginFormTitle')}</h2>
            <p className="mt-1 text-sm text-sky-100/62">{t('prototypeConnectionNotice')}</p>
          </div>
        <form onSubmit={handleSubmit} className="space-y-4" noValidate>
          <div>
            <label htmlFor="email" className="mb-2 block text-sm font-medium">Email</label>
            <input id="email" value={form.email} onChange={(e) => updateField('email', e.target.value)} className={`field-control ${errors.email ? 'field-error border-rose-300' : ''}`} />
            {errors.email && <p className="mt-1 text-sm text-rose-200">{errors.email}</p>}
          </div>
          <div>
            <label htmlFor="password" className="mb-2 block text-sm font-medium">{t('password')}</label>
            <PasswordField id="password" value={form.password} onChange={(e) => updateField('password', e.target.value)} visible={passwordVisible} onToggle={() => setPasswordVisible((value) => !value)} showLabel={t('showPassword')} hideLabel={t('hidePassword')} error={errors.password} autoComplete="current-password" />
            <div className="mt-2"><PasswordStrengthIndicator password={form.password} /></div>
            {errors.password && <p className="mt-1 text-sm text-rose-200">{errors.password}</p>}
          </div>
          <label className="flex min-h-11 items-center gap-3 rounded-2xl border border-sky-100/10 bg-sky-950/30 px-3 text-sm text-sky-100/80">
            <input type="checkbox" checked={form.remember} onChange={(e) => setForm({ ...form, remember: e.target.checked })} className="h-5 w-5 rounded border-sky-100/30 bg-sky-950 accent-primary" />
            {t('rememberSession')}
          </label>
          {message && <p className="rounded-xl border border-sky-100/10 bg-sky-400/10 p-3 text-sm text-sky-100">{message}</p>}
          {errors.form && <p className="field-error rounded-xl border border-rose-300/40 bg-rose-400/10 p-3 text-sm text-rose-100">{errors.form}</p>}
          <button disabled={loading} className="primary-button flex w-full items-center justify-center disabled:cursor-not-allowed">
            {loading ? <LoadingSpinner label={t('signingIn')} /> : t('signIn')}
          </button>
        </form>
        <div className="mt-5 rounded-xl bg-sky-400/10 p-3 text-xs text-sky-100/75">
          <p>{t('adminDemoCredentials')}</p>
          <p>{t('residentDemoCredentials')}</p>
        </div>
        <p className="mt-4 flex items-center gap-2 text-xs text-sky-100/65"><ShieldCheck size={14} /> {t('prototypeSecurityNotice')}</p>
        </div>
        <FooterSecurityBadge />
      </motion.section>
    </main>
  );
}
