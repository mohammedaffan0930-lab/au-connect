import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { api, getToken, setToken, loadMeta } from './api.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [meta, setMeta] = useState({ departments: [], complaintCategories: [] });
  const [loading, setLoading] = useState(true);

  const logout = useCallback(() => {
    setToken(null);
    setUser(null);
  }, []);

  useEffect(() => {
    loadMeta().then(setMeta).catch(() => {});
    if (!getToken()) {
      setLoading(false);
      return;
    }
    api
      .get('/users/me')
      .then(setUser)
      .catch(() => setToken(null))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    window.addEventListener('auth-expired', logout);
    return () => window.removeEventListener('auth-expired', logout);
  }, [logout]);

  const login = useCallback(async (email, password) => {
    const res = await api.post('/auth/login', { email, password });
    setToken(res.token);
    setUser(res.user);
    return res.user;
  }, []);

  const value = useMemo(
    () => ({ user, setUser, meta, loading, login, logout }),
    [user, meta, loading, login, logout]
  );
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);
