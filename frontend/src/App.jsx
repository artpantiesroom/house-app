import { Navigate, Route, Routes } from 'react-router-dom';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import { AuditProvider } from './context/AuditContext.jsx';
import { AuthProvider } from './context/AuthContext.jsx';
import { DataProvider } from './context/DataContext.jsx';
import { LanguageProvider } from './context/LanguageContext.jsx';
import AdminLayout from './layouts/AdminLayout.jsx';
import ResidentLayout from './layouts/ResidentLayout.jsx';
import Login from './pages/Login.jsx';
import ChangePassword from './pages/ChangePassword.jsx';
import Forbidden from './pages/Forbidden.jsx';
import Dashboard from './pages/admin/Dashboard.jsx';
import Residents from './pages/admin/Residents.jsx';
import Announcements from './pages/admin/Announcements.jsx';
import MaintenanceAdmin from './pages/admin/MaintenanceAdmin.jsx';
import Payments from './pages/admin/Payments.jsx';
import AuditLog from './pages/admin/AuditLog.jsx';
import Incidents from './pages/admin/Incidents.jsx';
import AdminContacts from './pages/admin/Contacts.jsx';
import Home from './pages/resident/Home.jsx';
import MyRequests from './pages/resident/MyRequests.jsx';
import MyPayments from './pages/resident/MyPayments.jsx';
import ResidentContacts from './pages/resident/Contacts.jsx';
import Profile from './pages/resident/Profile.jsx';

export default function App() {
  return (
    <LanguageProvider>
      <AuditProvider>
        <AuthProvider>
          <DataProvider>
            <Routes>
            <Route path="/" element={<Navigate to="/login" replace />} />
            <Route path="/login" element={<Login />} />
            <Route path="/forbidden" element={<Forbidden />} />
            <Route element={<ProtectedRoute allowedRoles={['ADMIN', 'RESIDENT']} allowPasswordChangeRequired />}>
              <Route path="/change-password" element={<ChangePassword />} />
            </Route>
            <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
              <Route path="/admin" element={<AdminLayout />}>
                <Route index element={<Navigate to="/admin/dashboard" replace />} />
                <Route path="dashboard" element={<Dashboard />} />
                <Route path="residents" element={<Residents />} />
                <Route path="announcements" element={<Announcements />} />
                <Route path="maintenance" element={<MaintenanceAdmin />} />
                <Route path="payments" element={<Payments />} />
                <Route path="audit-log" element={<AuditLog />} />
                <Route path="incidents" element={<Incidents />} />
                <Route path="contacts" element={<AdminContacts />} />
              </Route>
            </Route>
            <Route element={<ProtectedRoute allowedRoles={['RESIDENT']} />}>
              <Route path="/resident" element={<ResidentLayout />}>
                <Route index element={<Navigate to="/resident/home" replace />} />
                <Route path="home" element={<Home />} />
                <Route path="requests" element={<MyRequests />} />
                <Route path="payments" element={<MyPayments />} />
                <Route path="contacts" element={<ResidentContacts />} />
                <Route path="profile" element={<Profile />} />
              </Route>
            </Route>
            <Route path="*" element={<Navigate to="/login" replace />} />
            </Routes>
          </DataProvider>
        </AuthProvider>
      </AuditProvider>
    </LanguageProvider>
  );
}
