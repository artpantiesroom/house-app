import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { KeyRound } from 'lucide-react';
import LoadingSpinner from '../components/LoadingSpinner.jsx';
import PasswordStrengthIndicator, { getPasswordStrength } from '../components/PasswordStrengthIndicator.jsx';
import FooterSecurityBadge from '../components/FooterSecurityBadge.jsx';
import { useAuth } from '../context/AuthContext.jsx';

export default function ChangePassword() {
  const { user, authReady, changePassword, logout } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' });
  const [errors, setErrors] = useState({});
  const [saving, setSaving] = useState(false);

  if (!authReady) return <main className="grid min-h-screen place-items-center"><LoadingSpinner label="Відновлення сеансу" /></main>;
  if (!user) return <Navigate to="/login" replace />;
  if (!user.mustChangePassword) {
    return <Navigate to={user.role === 'ADMIN' ? '/admin/dashboard' : '/resident/home'} replace />;
  }

  const validate = () => {
    const next = {};
    if (!form.currentPassword) next.currentPassword = 'Поточний пароль обовʼязковий.';
    if (getPasswordStrength(form.newPassword) < 5) next.newPassword = 'Використайте щонайменше 8 символів: велику й малу літеру, цифру та символ.';
    if (form.newPassword !== form.confirmPassword) next.confirmPassword = 'Паролі мають збігатися.';
    setErrors(next);
    return Object.keys(next).length === 0;
  };

  const updateField = (field, value) => {
    setForm({ ...form, [field]: value });
    setErrors((current) => ({ ...current, [field]: '', form: '' }));
  };

  const submit = async (event) => {
    event.preventDefault();
    if (saving || !validate()) return;
    setSaving(true);
    try {
      const nextUser = await changePassword({ currentPassword: form.currentPassword, newPassword: form.newPassword });
      navigate(nextUser.role === 'ADMIN' ? '/admin/dashboard' : '/resident/home', { replace: true });
    } catch (error) {
      setErrors({ form: error.message || 'Не вдалося змінити пароль.' });
    } finally {
      setSaving(false);
    }
  };

  return (
    <main className="grid min-h-screen place-items-center p-4">
      <motion.section initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} className="glass w-full max-w-md rounded-2xl p-6">
        <div className="mb-6 flex items-center gap-3">
          <div className="grid h-12 w-12 place-items-center rounded-2xl bg-primary text-white"><KeyRound /></div>
          <div>
            <h1 className="text-2xl font-bold">Змініть пароль</h1>
            <p className="text-sm text-sky-100/70">Для продовження потрібен новий пароль.</p>
          </div>
        </div>
        <form onSubmit={submit} className="space-y-4" noValidate>
          <div>
            <label htmlFor="currentPassword" className="mb-2 block text-sm font-medium">Поточний пароль</label>
            <input id="currentPassword" type="password" value={form.currentPassword} onChange={(event) => updateField('currentPassword', event.target.value)} className={`focus-ring w-full rounded-xl border bg-sky-950/50 px-4 py-3 text-sky-50 ${errors.currentPassword ? 'field-error border-rose-300' : 'border-sky-100/15'}`} />
            {errors.currentPassword && <p className="mt-1 text-sm text-rose-200">{errors.currentPassword}</p>}
          </div>
          <div>
            <label htmlFor="newPassword" className="mb-2 block text-sm font-medium">Новий пароль</label>
            <input id="newPassword" type="password" value={form.newPassword} onChange={(event) => updateField('newPassword', event.target.value)} className={`focus-ring w-full rounded-xl border bg-sky-950/50 px-4 py-3 text-sky-50 ${errors.newPassword ? 'field-error border-rose-300' : 'border-sky-100/15'}`} />
            <div className="mt-2"><PasswordStrengthIndicator password={form.newPassword} /></div>
            {errors.newPassword && <p className="mt-1 text-sm text-rose-200">{errors.newPassword}</p>}
          </div>
          <div>
            <label htmlFor="confirmPassword" className="mb-2 block text-sm font-medium">Повторіть новий пароль</label>
            <input id="confirmPassword" type="password" value={form.confirmPassword} onChange={(event) => updateField('confirmPassword', event.target.value)} className={`focus-ring w-full rounded-xl border bg-sky-950/50 px-4 py-3 text-sky-50 ${errors.confirmPassword ? 'field-error border-rose-300' : 'border-sky-100/15'}`} />
            {errors.confirmPassword && <p className="mt-1 text-sm text-rose-200">{errors.confirmPassword}</p>}
          </div>
          {errors.form && <p className="field-error rounded-xl border border-rose-300/40 bg-rose-400/10 p-3 text-sm text-rose-100">{errors.form}</p>}
          <div className="grid gap-2 sm:grid-cols-[1fr_auto]">
            <button disabled={saving} className="focus-ring flex items-center justify-center rounded-xl bg-primary px-4 py-3 font-semibold text-white transition hover:scale-[1.02] disabled:cursor-not-allowed disabled:opacity-70">
              {saving ? <LoadingSpinner label="Збереження" /> : 'Змінити пароль'}
            </button>
            <button type="button" onClick={() => logout()} className="focus-ring rounded-xl border border-sky-100/20 px-4 py-3 text-sm font-semibold text-sky-100">Вийти</button>
          </div>
        </form>
        <FooterSecurityBadge />
      </motion.section>
    </main>
  );
}
