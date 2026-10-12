import { Navigate, Outlet, Route, Routes } from 'react-router-dom';
import { useAuth } from './auth.jsx';
import Navbar from './components/Navbar.jsx';
import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';
import Verify from './pages/Verify.jsx';
import Feed from './pages/Feed.jsx';
import Profile from './pages/Profile.jsx';
import HelpDesk from './pages/HelpDesk.jsx';
import Complaints from './pages/Complaints.jsx';
import Admin from './pages/Admin.jsx';

function Protected({ adminOnly = false }) {
  const { user, loading } = useAuth();
  if (loading) return <div className="center-note">Loading…</div>;
  if (!user) return <Navigate to="/login" replace />;
  if (adminOnly && user.role !== 'ADMIN') return <Navigate to="/" replace />;
  return (
    <>
      <Navbar />
      <main className="container">
        <Outlet />
      </main>
    </>
  );
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/verify" element={<Verify />} />

      <Route element={<Protected />}>
        <Route path="/" element={<Feed />} />
        <Route path="/profile" element={<Profile />} />
        <Route path="/profile/:id" element={<Profile />} />
        <Route path="/helpdesk" element={<HelpDesk />} />
        <Route path="/complaints" element={<Complaints />} />
      </Route>

      <Route element={<Protected adminOnly />}>
        <Route path="/admin" element={<Admin />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
