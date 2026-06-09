import { useState } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Building2, ShieldCheck } from 'lucide-react';
import LoadingSpinner from '../components/LoadingSpinner.jsx';
import PasswordStrengthIndicator, { getPasswordStrength } from '../components/PasswordStrengthIndicator.jsx';
import FooterSecurityBadge from '../components/FooterSecurityBadge.jsx';
import { sanitizeText } from '../data/mockData.js';
import { useAuth } from '../context/AuthContext.jsx';

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export default function Login() {
  const { user, authReady, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ email: '', password: '', remember: false });
  const [errors, setErrors] = useState({});
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);

  if (!authReady) return <main className="grid min-h-screen place-items-center"><LoadingSpinner label="Відновлення сеансу" /></main>;
  if (user?.mustChangePassword) return <Navigate to="/change-password" replace />;
  if (user?.role === 'ADMIN') return <Navigate to="/admin/dashboard" replace />;
  if (user?.role === 'RESIDENT') return <Navigate to="/resident/home" replace />;

  const validate = () => {
    const next = {};
    if (!emailPattern.test(form.email)) next.email = 'Введіть коректну email-адресу.';
    if (getPasswordStrength(form.password) < 5) next.password = 'Використайте щонайменше 8 символів: велику й малу літеру, цифру та символ.';
    setErrors(next);
    return Object.keys(next).length === 0;
  };

  const updateField = (field, value) => {
    setForm({ ...form, [field]: field === 'email' ? sanitizeText(value) : value });
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
      const loggedInUser = await login({ email: sanitizeText(form.email), password: form.password, remember: form.remember });
      const fallback = loggedInUser.mustChangePassword ? '/change-password' : loggedInUser.role === 'ADMIN' ? '/admin/dashboard' : '/resident/home';
      navigate(loggedInUser.mustChangePassword ? fallback : location.state?.from?.pathname || fallback, { replace: true });
    } catch (error) {
      setErrors({ form: error.message });
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="grid min-h-screen place-items-center p-4">
      <motion.section initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} className="glass w-full max-w-md rounded-2xl p-6">
        <div className="mb-6 flex items-center gap-3">
          <div className="grid h-12 w-12 place-items-center rounded-2xl bg-primary text-white"><Building2 /></div>
          <div>
            <h1 className="text-2xl font-bold">Azure Harbor</h1>
            <p className="text-sm text-sky-100/70">Прототип керування житловим будинком</p>
          </div>
        </div>
        <form onSubmit={handleSubmit} className="space-y-4" noValidate>
          <div>
            <label htmlFor="email" className="mb-2 block text-sm font-medium">Email</label>
            <input id="email" value={form.email} onChange={(e) => updateField('email', e.target.value)} className={`focus-ring w-full rounded-xl border bg-sky-950/50 px-4 py-3 text-sky-50 ${errors.email ? 'field-error border-rose-300' : 'border-sky-100/15'}`} />
            {errors.email && <p className="mt-1 text-sm text-rose-200">{errors.email}</p>}
          </div>
          <div>
            <label htmlFor="password" className="mb-2 block text-sm font-medium">Пароль</label>
            <input id="password" type="password" value={form.password} onChange={(e) => updateField('password', e.target.value)} className={`focus-ring w-full rounded-xl border bg-sky-950/50 px-4 py-3 text-sky-50 ${errors.password ? 'field-error border-rose-300' : 'border-sky-100/15'}`} />
            <div className="mt-2"><PasswordStrengthIndicator password={form.password} /></div>
            {errors.password && <p className="mt-1 text-sm text-rose-200">{errors.password}</p>}
          </div>
          <label className="flex items-center gap-2 text-sm text-sky-100/80">
            <input type="checkbox" checked={form.remember} onChange={(e) => setForm({ ...form, remember: e.target.checked })} className="h-4 w-4 rounded border-sky-100/30 bg-sky-950" />
            Запам'ятати сеанс
          </label>
          {message && <p className="rounded-xl border border-sky-100/10 bg-sky-400/10 p-3 text-sm text-sky-100">{message}</p>}
          {errors.form && <p className="field-error rounded-xl border border-rose-300/40 bg-rose-400/10 p-3 text-sm text-rose-100">{errors.form}</p>}
          <button disabled={loading} className="focus-ring flex w-full items-center justify-center rounded-xl bg-primary px-4 py-3 font-semibold text-white transition hover:scale-[1.02] disabled:cursor-not-allowed disabled:opacity-70">
            {loading ? <LoadingSpinner label="Вхід" /> : 'Увійти'}
          </button>
        </form>
        <div className="mt-5 rounded-xl bg-sky-400/10 p-3 text-xs text-sky-100/75">
          <p>Адміністратор: admin@house.com / Admin123!</p>
          <p>Мешканець: resident@house.com / Resident123!</p>
        </div>
        <p className="mt-4 flex items-center gap-2 text-xs text-sky-100/65"><ShieldCheck size={14} /> Контролі, натхненні ISO/IEC 27001, симулюються лише для цілей прототипу.</p>
        <FooterSecurityBadge />
      </motion.section>
    </main>
  );
}
