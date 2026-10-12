import { useCallback, useEffect, useState } from 'react';
import { api, timeAgo } from '../api.js';
import { useAuth } from '../auth.jsx';

export const STATUS_LABEL = {
  SUBMITTED: 'Submitted',
  UNDER_REVIEW: 'Under review',
  IN_PROGRESS: 'In progress',
  RESOLVED: 'Resolved',
};
const STEPS = ['SUBMITTED', 'UNDER_REVIEW', 'IN_PROGRESS', 'RESOLVED'];

export function PriorityBadge({ priority }) {
  return <span className={`badge prio prio-${priority.toLowerCase()}`}>{priority}</span>;
}

function StatusSteps({ status }) {
  const current = STEPS.indexOf(status);
  return (
    <ol className="steps">
      {STEPS.map((s, i) => (
        <li key={s} className={i < current ? 'done' : i === current ? 'current' : ''}>
          {STATUS_LABEL[s]}
        </li>
      ))}
    </ol>
  );
}

export default function Complaints() {
  const { meta } = useAuth();
  const catLabel = (c) => meta.complaintCategories.find((x) => x.code === c)?.label || c;

  const [tab, setTab] = useState('vote');
  const [open, setOpen] = useState([]);
  const [mine, setMine] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [form, setForm] = useState({ title: '', description: '', category: '' });
  const [busy, setBusy] = useState(false);
  const [sent, setSent] = useState(false);

  const load = useCallback(async () => {
    try {
      const [a, b] = await Promise.all([api.get('/complaints'), api.get('/complaints/mine')]);
      setOpen(a);
      setMine(b);
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
    setSent(false);
    try {
      await api.post('/complaints', form);
      setForm({ title: '', description: '', category: '' });
      setSent(true);
      await load();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  const vote = async (id) => {
    setError('');
    try {
      const r = await api.post(`/complaints/${id}/vote`);
      setOpen((list) =>
        list
          .map((c) => (c.id === id ? { ...c, votedByMe: r.voted, votes: r.votes, priority: r.priority } : c))
          .sort((a, b) => b.votes - a.votes)
      );
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="stack">
      <div>
        <h1 className="page-title">Complaint Desk</h1>
        <p className="muted">
          Raise a concern privately. Only admins can see who sent a complaint and its full details. Other students
          see just the title and can vote to push important issues up the list.
        </p>
      </div>

      <div className="chips">
        <button className={tab === 'vote' ? 'chip active' : 'chip'} onClick={() => setTab('vote')}>Vote on issues</button>
        <button className={tab === 'new' ? 'chip active' : 'chip'} onClick={() => setTab('new')}>Raise a concern</button>
        <button className={tab === 'mine' ? 'chip active' : 'chip'} onClick={() => setTab('mine')}>
          My complaints{mine.length ? ` (${mine.length})` : ''}
        </button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {loading && <div className="center-note">Loading…</div>}

      {tab === 'new' && (
        <section className="card">
          <form onSubmit={submit} className="form">
            <label>
              Short title
              <input value={form.title} onChange={set('title')} required maxLength={200}
                     placeholder="e.g. Water cooler on 3rd floor not working" />
            </label>
            <label>
              Category
              <select value={form.category} onChange={set('category')} required>
                <option value="">Choose a category</option>
                {meta.complaintCategories.map((c) => <option key={c.code} value={c.code}>{c.label}</option>)}
              </select>
            </label>
            <label>
              Details (visible only to you and admins)
              <textarea value={form.description} onChange={set('description')} required rows={6} maxLength={3000} />
            </label>
            {sent && <div className="alert alert-success">Your complaint was sent privately to the admins.</div>}
            <button className="btn btn-primary" disabled={busy}>{busy ? 'Sending…' : 'Submit complaint'}</button>
          </form>
        </section>
      )}

      {tab === 'vote' && !loading && (
        <div className="stack">
          {open.length === 0 && <div className="card empty">No complaints have been raised yet.</div>}
          {open.map((c) => (
            <article key={c.id} className="card complaint">
              <div className="vote-col">
                <button
                  className={c.votedByMe ? 'vote-btn voted' : 'vote-btn'}
                  onClick={() => vote(c.id)}
                  disabled={c.mine}
                  title={c.mine ? 'You cannot vote on your own complaint' : c.votedByMe ? 'Remove your vote' : 'This affects me too'}
                  aria-label={c.votedByMe ? 'Remove your vote' : 'Vote for this complaint'}
                >
                  ▲
                </button>
                <strong>{c.votes}</strong>
              </div>
              <div className="grow">
                <div className="item-tags">
                  <span className="badge">{catLabel(c.category)}</span>
                  <PriorityBadge priority={c.priority} />
                  <span className={`badge status status-${c.status.toLowerCase()}`}>{STATUS_LABEL[c.status]}</span>
                  <span className="muted small">{timeAgo(c.createdAt)}</span>
                </div>
                <h4>{c.title}</h4>
                {c.mine && <div className="muted small">This is your complaint.</div>}
              </div>
            </article>
          ))}
        </div>
      )}

      {tab === 'mine' && !loading && (
        <div className="stack">
          {mine.length === 0 && <div className="card empty">You haven't submitted any complaints.</div>}
          {mine.map((c) => (
            <article key={c.id} className="card">
              <div className="item-tags">
                <span className="badge">{catLabel(c.category)}</span>
                <PriorityBadge priority={c.priority} />
                <span className="muted small">{c.votes} student vote{c.votes === 1 ? '' : 's'} · {timeAgo(c.createdAt)}</span>
              </div>
              <h4>{c.title}</h4>
              <p>{c.description}</p>
              <StatusSteps status={c.status} />
              {c.adminNote && (
                <div className="alert alert-info"><strong>Admin response:</strong> {c.adminNote}</div>
              )}
            </article>
          ))}
        </div>
      )}
    </div>
  );
}
