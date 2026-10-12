import { useCallback, useEffect, useState } from 'react';
import { api, timeAgo } from '../api.js';
import { useAuth } from '../auth.jsx';
import { PriorityBadge, STATUS_LABEL } from './Complaints.jsx';

function ComplaintRow({ complaint, catLabel, onSaved }) {
  const [status, setStatus] = useState(complaint.status);
  const [note, setNote] = useState(complaint.adminNote || '');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const dirty = status !== complaint.status || note !== (complaint.adminNote || '');

  const save = async () => {
    setBusy(true);
    setError('');
    try {
      onSaved(await api.put('/admin/complaints/' + complaint.id, { status, adminNote: note }));
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <article className="card complaint-admin">
      <div className="item-tags">
        <span className="badge">{catLabel(complaint.category)}</span>
        <PriorityBadge priority={complaint.priority} />
        <span className="badge">▲ {complaint.votes}</span>
        <span className="muted small">{timeAgo(complaint.createdAt)}</span>
      </div>
      <h4>{complaint.title}</h4>
      <p>{complaint.description}</p>
      <div className="muted small">
        From {complaint.authorName} ({complaint.authorDepartment}) ·{' '}
        <a href={`mailto:${complaint.authorEmail}`}>{complaint.authorEmail}</a>
      </div>
      <div className="admin-controls">
        <select value={status} onChange={(e) => setStatus(e.target.value)} aria-label="Status">
          {Object.entries(STATUS_LABEL).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
        </select>
        <input value={note} onChange={(e) => setNote(e.target.value)} maxLength={1000}
               placeholder="Response shown to the student (optional)" />
        <button className="btn btn-primary btn-sm" onClick={save} disabled={busy || !dirty}>
          {busy ? 'Saving…' : 'Save'}
        </button>
      </div>
      {error && <div className="alert alert-error">{error}</div>}
    </article>
  );
}

export default function Admin() {
  const { meta } = useAuth();
  const catLabel = (c) => meta.complaintCategories.find((x) => x.code === c)?.label || c;
  const [stats, setStats] = useState(null);
  const [list, setList] = useState([]);
  const [filter, setFilter] = useState('ACTIVE');
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    try {
      const [s, c] = await Promise.all([api.get('/admin/stats'), api.get('/admin/complaints')]);
      setStats(s);
      setList(c);
    } catch (e) {
      setError(e.message);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  const onSaved = (updated) => {
    setList((l) => l.map((c) => (c.id === updated.id ? updated : c)));
    api.get('/admin/stats').then(setStats).catch(() => {});
  };

  const shown = list.filter((c) =>
    filter === 'ALL' ? true : filter === 'ACTIVE' ? c.status !== 'RESOLVED' : c.status === filter
  );

  return (
    <div className="stack">
      <h1 className="page-title">Admin dashboard</h1>
      {error && <div className="alert alert-error">{error}</div>}

      {stats && (
        <div className="stats">
          <div className="card stat"><strong>{stats.users}</strong><span>Members</span></div>
          <div className="card stat"><strong>{stats.posts}</strong><span>Posts</span></div>
          <div className="card stat"><strong>{stats.openHelpItems}</strong><span>Open lost &amp; found</span></div>
          <div className="card stat"><strong>{stats.complaintsUnresolved}</strong><span>Unresolved complaints</span></div>
        </div>
      )}

      <div className="chips">
        {[['ACTIVE', 'Needs attention'], ['ALL', 'All'], ...Object.entries(STATUS_LABEL)].map(([k, v]) => (
          <button key={k} className={filter === k ? 'chip active' : 'chip'} onClick={() => setFilter(k)}>{v}</button>
        ))}
      </div>
      <p className="muted small">Sorted by student votes, so the issues affecting the most people appear first.</p>

      {shown.length === 0 && <div className="card empty">No complaints in this view.</div>}
      {shown.map((c) => (
        <ComplaintRow key={c.id} complaint={c} catLabel={catLabel} onSaved={onSaved} />
      ))}
    </div>
  );
}
