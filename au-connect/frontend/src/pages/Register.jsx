import { useState } from 'react';
import { Link, Navigate } from 'react-router-dom';
import { useAuth } from '../auth.jsx';
import { api } from '../api.js';
import AuthLayout from './AuthLayout.jsx';

export default function Register() {
  const { user, meta } = useAuth();
  const [form, setForm] = useState({
    fullName: '', email: '', password: '', department: '', role: 'STUDENT', yearOfStudy: 1,
  });
  const [error, setError] = useState('');
  const [result, setResult] = useState(null);
  const [busy, setBusy] = useState(false);

  if (user) return <Navigate to="/" replace />;

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });
  const departments = meta.departments.filter((d) => d.code !== 'ALL');

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setBusy(true);
    try {
      const body = {
        ...form,
        email: form.email.trim(),
        yearOfStudy: form.role === 'STUDENT' ? Number(form.yearOfStudy) : null,
      };
      setResult(await api.post('/auth/register', body));
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  if (result) {
    return (
      <AuthLayout title="Check your email">
        <div className="alert alert-success">{result.message}</div>
        {result.devVerificationLink && (
          <p><a className="btn btn-primary" href={result.devVerificationLink}>Verify my email now</a></p>
        )}
        <p className="auth-switch"><Link to="/login">Go to log in</Link></p>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout title="Create your account" subtitle="Only Anurag University email addresses can register.">
      <form onSubmit={submit} className="form">
        <label>
          Full name
          <input value={form.fullName} onChange={set('fullName')} required maxLength={100} />
        </label>
        <label>
          University email
          <input type="email" value={form.email} onChange={set('email')} required
                 placeholder="rollno@anurag.edu.in" autoComplete="username" />
        </label>
        <label>
          Password (min 8 characters)
          <input type="password" value={form.password} onChange={set('password')} required minLength={8}
                 autoComplete="new-password" />
        </label>
        <div className="row">
          <label className="grow">
            I am a
            <select value={form.role} onChange={set('role')}>
              <option value="STUDENT">Student</option>
              <option value="FACULTY">Faculty</option>
            </select>
          </label>
          {form.role === 'STUDENT' && (
            <label>
              Year
              <select value={form.yearOfStudy} onChange={set('yearOfStudy')}>
                {[1, 2, 3, 4, 5, 6].map((y) => <option key={y} value={y}>{y}</option>)}
              </select>
            </label>
          )}
        </div>
        <label>
          Department
          <select value={form.department} onChange={set('department')} required>
            <option value="">Select your department</option>
            {departments.map((d) => <option key={d.code} value={d.code}>{d.label}</option>)}
          </select>
        </label>
        {error && <div className="alert alert-error">{error}</div>}
        <button className="btn btn-primary" disabled={busy}>{busy ? 'Creating…' : 'Create account'}</button>
      </form>
      <p className="auth-switch">Already registered? <Link to="/login">Log in</Link></p>
    </AuthLayout>
  );
}
