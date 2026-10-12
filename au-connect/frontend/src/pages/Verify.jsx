import { useEffect, useRef, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { api } from '../api.js';
import AuthLayout from './AuthLayout.jsx';

export default function Verify() {
  const [params] = useSearchParams();
  const token = params.get('token');
  const [state, setState] = useState({ status: token ? 'loading' : 'error', message: token ? '' : 'Missing verification token.' });
  const started = useRef(false);

  useEffect(() => {
    if (!token || started.current) return; // a token is single-use, so only call once
    started.current = true;
    api
      .get('/auth/verify?token=' + encodeURIComponent(token))
      .then((r) => setState({ status: 'ok', message: r.message }))
      .catch((e) => setState({ status: 'error', message: e.message }));
  }, [token]);

  return (
    <AuthLayout title="Email verification">
      {state.status === 'loading' && <p className="muted">Verifying your email…</p>}
      {state.status === 'ok' && <div className="alert alert-success">{state.message}</div>}
      {state.status === 'error' && <div className="alert alert-error">{state.message}</div>}
      {state.status !== 'loading' && (
        <p className="auth-switch"><Link className="btn btn-primary" to="/login">Go to log in</Link></p>
      )}
    </AuthLayout>
  );
}
