import { v4 as uuid } from 'uuid';

export const simulateRequest = (data, delay = 800) =>
  new Promise((resolve) => setTimeout(() => resolve(data), delay));

export const sanitizeText = (value) => String(value || '').replace(/[<>"]/g, '').trim();

export const demoUsers = [
  {
    id: 'admin-1',
    name: 'Мая Стоун',
    email: 'admin@house.com',
    role: 'Administrator',
  },
  {
    id: 'resident-1',
    residentId: 'res-1',
    name: 'Алекс Рівера',
    email: 'resident@house.com',
    role: 'Resident',
  },
];

export const initialResidents = [
  { id: 'res-1', name: 'Алекс Рівера', email: 'resident@house.com', phone: '555-0141', apartment: '101', floor: 1 },
  { id: 'res-2', name: 'Прія Шах', email: 'priya@house.com', phone: '555-0142', apartment: '204', floor: 2 },
  { id: 'res-3', name: 'Ноа Чен', email: 'noah@house.com', phone: '555-0143', apartment: '205', floor: 2 },
  { id: 'res-4', name: 'Елена Брукс', email: 'elena@house.com', phone: '555-0144', apartment: '306', floor: 3 },
  { id: 'res-5', name: 'Сем Вілсон', email: 'sam@house.com', phone: '555-0145', apartment: '310', floor: 3 },
];

export const initialAnnouncements = [
  { id: 'ann-1', title: 'Миття вікон у лобі', body: 'Вікна в лобі митимуть у пʼятницю з 09:00 до 12:00.', date: '2026-06-01', audience: 'Усі мешканці' },
  { id: 'ann-2', title: 'Перевірка ліфта', body: 'Ліфт B буде недоступний у середу вранці через щорічну перевірку.', date: '2026-06-02', audience: 'Усі мешканці' },
  { id: 'ann-3', title: 'Оновлено години роботи даху', body: 'Тиха година на даху тепер починається щодня о 21:00.', date: '2026-06-03', audience: 'Усі мешканці' },
];

export const initialMaintenanceRequests = [
  { id: 'req-1', residentId: 'res-1', title: 'Протікає кухонна мийка', category: 'Сантехніка', details: 'Повільне протікання під кухонною мийкою.', status: 'Open', createdAt: '2026-05-28T10:20:00.000Z' },
  { id: 'req-2', residentId: 'res-2', title: 'Не працює світло в коридорі', category: 'Електрика', details: 'Лампочка біля квартири 204 мерехтить.', status: 'In Progress', createdAt: '2026-05-29T14:10:00.000Z' },
  { id: 'req-3', residentId: 'res-4', title: 'Шумить вентиляція', category: 'Клімат-система', details: 'Блок вночі видає деренчливий звук.', status: 'Resolved', createdAt: '2026-05-22T08:30:00.000Z' },
  { id: 'req-4', residentId: 'res-5', title: 'Пульт від гаража', category: 'Доступ', details: 'Пульт перестав відкривати ворота гаража.', status: 'Rejected', createdAt: '2026-05-25T16:05:00.000Z' },
];

export const initialPayments = initialResidents.flatMap((resident) => [
  { id: `pay-${resident.id}-may`, residentId: resident.id, month: 'Травень 2026', amount: 1850, status: resident.id === 'res-3' ? 'Overdue' : 'Paid', dueDate: '2026-05-05' },
  { id: `pay-${resident.id}-jun`, residentId: resident.id, month: 'Червень 2026', amount: 1850, status: resident.id === 'res-1' || resident.id === 'res-5' ? 'Unpaid' : 'Paid', dueDate: '2026-06-05' },
]);

export const securityIncidents = [
  { id: 'inc-1', title: 'Повторні невдалі спроби входу', severity: 'Medium', timestamp: '2026-06-01T12:45:00.000Z', recommendedAction: 'Перевірити активність облікового запису та залишити симуляцію блокування увімкненою.', status: 'Monitoring' },
  { id: 'inc-2', title: 'Панель доступу дверей офлайн', severity: 'High', timestamp: '2026-06-02T07:15:00.000Z', recommendedAction: 'Направити команду обслуговування та перевірити ручну процедуру доступу.', status: 'Open' },
  { id: 'inc-3', title: 'Застарілі контакти мешканця', severity: 'Low', timestamp: '2026-06-03T09:00:00.000Z', recommendedAction: 'Попросити службу мешканців підтвердити контактні дані.', status: 'Resolved' },
];

export const initialAuditLog = [
  { id: uuid(), timestamp: '2026-05-30T09:00:00.000Z', actor: 'system@house.com', action: 'SESSION_RESTORE', target: 'Auth', result: 'SUCCESS' },
  { id: uuid(), timestamp: '2026-05-31T10:10:00.000Z', actor: 'admin@house.com', action: 'ANNOUNCEMENT_CREATED', target: 'ann-1', result: 'SUCCESS' },
  { id: uuid(), timestamp: '2026-06-01T08:25:00.000Z', actor: 'admin@house.com', action: 'MAINTENANCE_STATUS_CHANGED', target: 'req-2', result: 'SUCCESS' },
  { id: uuid(), timestamp: '2026-06-01T12:45:00.000Z', actor: 'resident@house.com', action: 'LOGIN', target: 'Auth', result: 'SUCCESS' },
  { id: uuid(), timestamp: '2026-06-02T17:20:00.000Z', actor: 'resident@house.com', action: 'REQUEST_CREATED', target: 'Maintenance', result: 'SUCCESS' },
];

export const buildingContacts = [
  { id: 'con-1', label: 'Ресепшн', value: '555-0100', classification: 'Public' },
  { id: 'con-2', label: 'Аварійне обслуговування', value: '555-0199', classification: 'Public' },
  { id: 'con-3', label: 'Керуючий будинком', value: 'manager@house.com', classification: 'Internal' },
  { id: 'con-4', label: 'Служба безпеки', value: 'security@house.com', classification: 'Internal' },
];

export const buildingInfo = {
  name: 'Резиденція Azure Harbor',
  address: '1200 Ocean View Lane',
  floors: 3,
  quietHours: '21:00 - 07:00',
  policyNote: 'Контролі, натхненні ISO/IEC 27001, симулюються лише для цілей прототипу.',
};
