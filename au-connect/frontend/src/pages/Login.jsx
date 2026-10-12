import { useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth.jsx';
import { api } from '../api.js';
import AuthLayout from './AuthLayout.jsx';

export default function Login() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [notVerified, setNotVerified] = useState(false);
  const [info, setInfo] = useState(null);
  const [busy, setBusy] = useState(false);

  if (user) return <Navigate to="/" replace />;

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setInfo(null);
    setNotVerified(false);
    setBusy(true);
    try {
      await login(email.trim(), password);
      navigate('/');
    } catch (err) {
      setError(err.message);
      setNotVerified(err.status === 403);
    } finally {
      setBusy(false);
    }
  };

  const resend = async () => {
    try {
      setInfo(await api.post('/auth/resend', { email: email.trim() }));
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <AuthLayout title="Log in" subtitle="Use your @anurag.edu.in email address.">
      <form onSubmit={submit} className="form">
        <label>
          University email
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required autoFocus
                 placeholder="rollno@anurag.edu.in" autoComplete="username" />
        </label>
        <label>
          Password
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required
                 autoComplete="current-password" />
        </label>
        {error && <div className="alert alert-error">{error}</div>}
        {notVerified && (
          <button type="button" className="btn btn-outline" onClick={resend}>Resend verification email</button>
        )}
        {info && (
          <div className="alert alert-info">
            {info.message}
            {info.devVerificationLink && (
              <div><a href={info.devVerificationLink}>Verify my email now</a></div>
            )}
          </div>
        )}
        <button className="btn btn-primary" disabled={busy}>{busy ? 'Logging in…' : 'Log in'}</button>
      </form>
      <p className="auth-switch">New to AU Connect? <Link to="/register">Create an account</Link></p>
    </AuthLayout>
  );
}
