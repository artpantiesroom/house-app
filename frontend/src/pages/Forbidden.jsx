import { Link } from 'react-router-dom';
import { ShieldX } from 'lucide-react';

export default function Forbidden() {
  return (
    <main className="grid min-h-screen place-items-center p-4">
      <section className="glass max-w-md rounded-2xl p-6 text-center">
        <ShieldX className="mx-auto mb-4 text-rose-200" size={46} />
        <h1 className="text-2xl font-bold">Доступ заборонено</h1>
        <p className="mt-3 text-sky-100/75">Ваша роль не має дозволу на перегляд цієї зони. Спробу доступу записано в симульований журнал аудиту.</p>
        <Link to="/login" className="focus-ring mt-5 inline-flex rounded-xl bg-primary px-5 py-3 font-semibold text-white">Повернутися до входу</Link>
      </section>
    </main>
  );
}
