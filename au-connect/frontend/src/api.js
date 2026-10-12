const TOKEN_KEY = 'auconnect_token';

export const getToken = () => localStorage.getItem(TOKEN_KEY);
export const setToken = (t) =>
  t ? localStorage.setItem(TOKEN_KEY, t) : localStorage.removeItem(TOKEN_KEY);

async function request(path, { method = 'GET', body, form } = {}) {
  const headers = {};
  const token = getToken();
  if (token) headers.Authorization = 'Bearer ' + token;

  let payload;
  if (form) {
    payload = form; // the browser sets the multipart boundary itself
  } else if (body !== undefined) {
    headers['Content-Type'] = 'application/json';
    payload = JSON.stringify(body);
  }

  const res = await fetch('/api' + path, { method, headers, body: payload });
  const text = await res.text();
  let data = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = null;
    }
  }

  if (!res.ok) {
    if (res.status === 401 && token) window.dispatchEvent(new Event('auth-expired'));
    const err = new Error((data && data.message) || `Request failed (${res.status})`);
    err.status = res.status;
    throw err;
  }
  return data;
}

export const api = {
  get: (p) => request(p),
  post: (p, body) => request(p, { method: 'POST', body }),
  put: (p, body) => request(p, { method: 'PUT', body }),
  del: (p) => request(p, { method: 'DELETE' }),
  postForm: (p, form) => request(p, { method: 'POST', form }),
};

// Departments and complaint categories come from the backend so both sides stay in sync.
let metaPromise;
export function loadMeta() {
  if (!metaPromise) metaPromise = api.get('/meta').catch((e) => { metaPromise = null; throw e; });
  return metaPromise;
}

export function timeAgo(iso) {
  const secs = Math.max(1, Math.floor((Date.now() - new Date(iso).getTime()) / 1000));
  if (secs < 60) return 'just now';
  const mins = Math.floor(secs / 60);
  if (mins < 60) return `${mins}m ago`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs}h ago`;
  const days = Math.floor(hrs / 24);
  if (days < 7) return `${days}d ago`;
  return new Date(iso).toLocaleDateString();
}
