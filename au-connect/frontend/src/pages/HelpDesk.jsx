import { useCallback, useEffect, useRef, useState } from 'react';
import { api, timeAgo } from '../api.js';
import { useAuth } from '../auth.jsx';

const EMPTY = { type: 'LOST', title: '', description: '', location: '' };

export default function HelpDesk() {
  const { user } = useAuth();
  const [items, setItems] = useState([]);
  const [filter, setFilter] = useState('ALL');
  const [showResolved, setShowResolved] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [form, setForm] = useState(EMPTY);
  const [photo, setPhoto] = useState(null);
  const [busy, setBusy] = useState(false);
  const fileRef = useRef(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      setItems(await api.get('/helpdesk'));
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError('');
    try {
      const data = new FormData();
      Object.entries(form).forEach(([k, v]) => data.append(k, v));
      if (photo) data.append('photo', photo);
      await api.postForm('/helpdesk', data);
      setForm(EMPTY);
      setPhoto(null);
      if (fileRef.current) fileRef.current.value = '';
      await load();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  const resolve = async (id) => {
    try {
      const updated = await api.put(`/helpdesk/${id}/resolve`);
      setItems((list) => list.map((x) => (x.id === id ? updated : x)));
    } catch (err) {
      setError(err.message);
    }
  };

  const remove = async (id) => {
    if (!window.confirm('Delete this report?')) return;
    try {
      await api.del('/helpdesk/' + id);
      setItems((list) => list.filter((x) => x.id !== id));
    } catch (err) {
      setError(err.message);
    }
  };

  const visible = items.filter(
    (i) => (filter === 'ALL' || i.type === filter) && (showResolved || i.status === 'OPEN')
  );

  return (
    <div className="stack">
      <div>
        <h1 className="page-title">Help Desk</h1>
        <p className="muted">Lost something on campus, or found something that isn't yours? Report it here.</p>
      </div>

      <section className="card">
        <h3>Report an item</h3>
        <form onSubmit={submit} className="form">
          <div className="row">
            <label>
              I
              <select value={form.type} onChange={set('type')}>
                <option value="LOST">lost something</option>
                <option value="FOUND">found something</option>
              </select>
            </label>
            <label className="grow">
              What is it?
              <input value={form.title} onChange={set('title')} required maxLength={150}
                     placeholder="e.g. Black umbrella, ID card, water bottle" />
            </label>
          </div>
          <label>
            Where (and when)?
            <input value={form.location} onChange={set('location')} maxLength={255}
                   placeholder="e.g. Library, 2nd floor, yesterday afternoon" />
          </label>
          <label>
            Details
            <textarea value={form.description} onChange={set('description')} rows={3} maxLength={1000}
                      placeholder="Colour, brand, anything that helps identify it" />
          </label>
          <div className="composer-actions">
            <label className="btn btn-outline btn-sm file-btn">
              {photo ? 'Photo: ' + photo.name.slice(0, 20) : 'Add photo'}
              <input ref={fileRef} type="file" accept="image/jpeg,image/png,image/webp,image/gif"
                     onChange={(e) => setPhoto(e.target.files[0] || null)} hidden />
            </label>
            <button className="btn btn-primary push-right" disabled={busy}>{busy ? 'Submitting…' : 'Submit report'}</button>
          </div>
        </form>
      </section>

      <div className="chips">
        {['ALL', 'LOST', 'FOUND'].map((f) => (
          <button key={f} className={filter === f ? 'chip active' : 'chip'} onClick={() => setFilter(f)}>
            {f === 'ALL' ? 'All' : f === 'LOST' ? 'Lost' : 'Found'}
          </button>
        ))}
        <label className="chip-check">
          <input type="checkbox" checked={showResolved} onChange={(e) => setShowResolved(e.target.checked)} />
          Show resolved
        </label>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {loading && <div className="center-note">Loading…</div>}
      {!loading && visible.length === 0 && <div className="card empty">No reports to show.</div>}

      <div className="grid">
        {visible.map((i) => (
          <article key={i.id} className={i.status === 'RESOLVED' ? 'card item resolved' : 'card item'}>
            {i.photoUrl && <img className="item-photo" src={i.photoUrl} alt={i.title} loading="lazy" />}
            <div className="item-body">
              <div className="item-tags">
                <span className={i.type === 'LOST' ? 'badge badge-lost' : 'badge badge-found'}>{i.type}</span>
                {i.status === 'RESOLVED' && <span className="badge">Resolved</span>}
                <span className="muted small">{timeAgo(i.createdAt)}</span>
              </div>
              <h4>{i.title}</h4>
              {i.location && <div className="small">📍 {i.location}</div>}
              {i.description && <p>{i.description}</p>}
              <div className="muted small">
                Reported by {i.reporterName} · <a href={`mailto:${i.reporterEmail}`}>{i.reporterEmail}</a>
              </div>
              {(i.reporterId === user.id || user.role === 'ADMIN') && (
                <div className="item-actions">
                  {i.status === 'OPEN' && (
                    <button className="btn btn-outline btn-sm" onClick={() => resolve(i.id)}>Mark resolved</button>
                  )}
                  <button className="btn btn-ghost btn-sm" onClick={() => remove(i.id)}>Delete</button>
                </div>
              )}
            </div>
          </article>
        ))}
      </div>
    </div>
  );
}
