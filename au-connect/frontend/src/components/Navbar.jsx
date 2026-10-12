import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth.jsx';
import Logo from './Logo.jsx';
import Avatar from './Avatar.jsx';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const onLogout = () => {
    logout();
    navigate('/login');
  };
  const link = ({ isActive }) => (isActive ? 'nav-link active' : 'nav-link');

  return (
    <header className="navbar">
      <div className="navbar-inner">
        <NavLink to="/" className="brand">
          <Logo size={36} />
          <span>AU Connect</span>
        </NavLink>
        <nav className="nav-links">
          <NavLink to="/" end className={link}>Feed</NavLink>
          <NavLink to="/helpdesk" className={link}>Help Desk</NavLink>
          <NavLink to="/complaints" className={link}>Complaints</NavLink>
          {user.role === 'ADMIN' && <NavLink to="/admin" className={link}>Admin</NavLink>}
        </nav>
        <div className="nav-user">
          <NavLink to="/profile" className="nav-profile" title="My profile">
            <Avatar name={user.fullName} url={user.photoUrl} size={32} />
            <span className="nav-name">{user.fullName.split(' ')[0]}</span>
          </NavLink>
          <button className="btn btn-ghost btn-sm" onClick={onLogout}>Log out</button>
        </div>
      </div>
    </header>
  );
}
