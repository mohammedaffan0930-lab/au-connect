import { useEffect, useRef, useState } from 'react';
import { useParams } from 'react-router-dom';
import { api } from '../api.js';
import { useAuth } from '../auth.jsx';
import Avatar from '../components/Avatar.jsx';

export default function Profile() {
  const { id } = useParams();
  const { user: me, setUser, meta } = useAuth();
  const isMine = !id || Number(id) === me.id;

  const [profile, setProfile] = useState(isMine ? me : null);
  const [form, setForm] = useState(null);
  const [error, setError] = useState('');
  const [saved, setSaved] = useState(false);
  const [busy, setBusy] = useState(false);
  const fileRef = useRef(null);

  const labelOf = (code) => meta.departments.find((d) => d.code === code)?.label || code;

  useEffect(() => {
    setError('');
    setSaved(false);
    if (isMine) {
      setProfile(me);
      return;
    }
    setProfile(null);
    api.get('/users/' + id).then(setProfile).catch((e) => setError(e.message));
  }, [id, isMine, me]);

  useEffect(() => {
    if (isMine && profile) {
      setForm({
        fullName: profile.fullName,
        department: profile.department || '',
        yearOfStudy: profile.yearOfStudy || 1,
        bio: profile.bio || '',
      });
    }
  }, [isMine, profile]);

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const save = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError('');
    setSaved(false);
    try {
      const body = {
        ...form,
        yearOfStudy: me.role === 'STUDENT' ? Number(form.yearOfStudy) : null,
      };
      const updated = await api.put('/users/me', body);
      setUser(updated);
      setSaved(true);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  const uploadPhoto = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    setError('');
    try {
      const data = new FormData();
      data.append('photo', file);
      setUser(await api.postForm('/users/me/photo', data));
    } catch (err) {
      setError(err.message);
    } finally {
      if (fileRef.current) fileRef.current.value = '';
    }
  };

  if (!profile) {
    return error ? <div className="alert alert-error">{error}</div> : <div className="center-note">Loading…</div>;
  }

  return (
    <div className="card profile">
      <div className="profile-head">
        <Avatar name={profile.fullName} url={profile.photoUrl} size={96} />
        <div>
          <h2>{profile.fullName}</h2>
          <div className="muted">
            <span className="badge">{profile.role}</span>{' '}
            {labelOf(profile.department)}
            {profile.role === 'STUDENT' && profile.yearOfStudy ? ` · Year ${profile.yearOfStudy}` : ''}
          </div>
          {profile.email && <div className="muted small">{profile.email}</div>}
          {isMine && (
            <label className="btn btn-outline btn-sm file-btn mt">
              Change photo
              <input ref={fileRef} type="file" accept="image/jpeg,image/png,image/webp,image/gif" onChange={uploadPhoto} hidden />
            </label>
          )}
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {isMine && form ? (
        <form onSubmit={save} className="form">
          <label>
            Full name
            <input value={form.fullName} onChange={set('fullName')} required maxLength={100} />
          </label>
          <div className="row">
            <label className="grow">
              Department
              <select value={form.department} onChange={set('department')} required>
                {meta.departments.map((d) => <option key={d.code} value={d.code}>{d.label}</option>)}
              </select>
            </label>
            {me.role === 'STUDENT' && (
              <label>
                Year
                <select value={form.yearOfStudy} onChange={set('yearOfStudy')}>
                  {[1, 2, 3, 4, 5, 6].map((y) => <option key={y} value={y}>{y}</option>)}
                </select>
              </label>
            )}
          </div>
          <label>
            About me
            <textarea value={form.bio} onChange={set('bio')} rows={4} maxLength={500}
                      placeholder="Tell your campus a little about yourself" />
          </label>
          {saved && <div className="alert alert-success">Profile saved.</div>}
          <button className="btn btn-primary" disabled={busy}>{busy ? 'Saving…' : 'Save changes'}</button>
        </form>
      ) : (
        <p className="profile-bio">{profile.bio || <span className="muted">No bio yet.</span>}</p>
      )}
    </div>
  );
}
