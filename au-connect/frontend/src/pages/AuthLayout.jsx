import Logo from '../components/Logo.jsx';

/** Shared centred card for the login, register and verify pages. */
export default function AuthLayout({ title, subtitle, children }) {
  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-brand">
          <Logo size={64} />
          <h1>AU Connect</h1>
          <p className="muted">Anurag University campus community</p>
        </div>
        <h2>{title}</h2>
        {subtitle && <p className="muted">{subtitle}</p>}
        {children}
      </div>
    </div>
  );
}
