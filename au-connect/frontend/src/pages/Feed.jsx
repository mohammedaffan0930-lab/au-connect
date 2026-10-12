import { useCallback, useEffect, useRef, useState } from 'react';
import { api } from '../api.js';
import { useAuth } from '../auth.jsx';
import PostCard from '../components/PostCard.jsx';
import Avatar from '../components/Avatar.jsx';

export default function Feed() {
  const { user, meta } = useAuth();
  const labelOf = (code) => meta.departments.find((d) => d.code === code)?.label || code;

  // The feed starts on the user's own department; "All" shows everything.
  const [dept, setDept] = useState(user.department && user.department !== 'ALL' ? user.department : 'ALL');
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [content, setContent] = useState('');
  const [postDept, setPostDept] = useState(user.department || 'ALL');
  const [type, setType] = useState('POST');
  const [photo, setPhoto] = useState(null);
  const [preview, setPreview] = useState(null);
  const [posting, setPosting] = useState(false);
  const fileRef = useRef(null);

  const canNotice = user.role !== 'STUDENT';

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      setPosts(await api.get('/posts?department=' + encodeURIComponent(dept)));
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, [dept]);

  useEffect(() => { load(); }, [load]);

  const pickPhoto = (e) => {
    const f = e.target.files[0];
    setPhoto(f || null);
    setPreview(f ? URL.createObjectURL(f) : null);
  };

  const clearPhoto = () => {
    setPhoto(null);
    setPreview(null);
    if (fileRef.current) fileRef.current.value = '';
  };

  const submit = async (e) => {
    e.preventDefault();
    if (!content.trim()) return;
    setPosting(true);
    setError('');
    try {
      const form = new FormData();
      form.append('content', content);
      form.append('department', postDept);
      form.append('type', canNotice ? type : 'POST');
      if (photo) form.append('photo', photo);
      await api.postForm('/posts', form);
      setContent('');
      setType('POST');
      clearPhoto();
      await load();
    } catch (err) {
      setError(err.message);
    } finally {
      setPosting(false);
    }
  };

  const remove = async (id) => {
    if (!window.confirm('Delete this post?')) return;
    try {
      await api.del('/posts/' + id);
      setPosts((p) => p.filter((x) => x.id !== id));
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="stack">
      <section className="card composer">
        <form onSubmit={submit} className="form">
          <div className="composer-top">
            <Avatar name={user.fullName} url={user.photoUrl} size={42} />
            <textarea
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder={`Share something with your campus, ${user.fullName.split(' ')[0]}…`}
              maxLength={2000}
              rows={3}
            />
          </div>
          {preview && (
            <div className="preview">
              <img src={preview} alt="Selected upload preview" />
              <button type="button" className="btn btn-ghost btn-sm" onClick={clearPhoto}>Remove photo</button>
            </div>
          )}
          <div className="composer-actions">
            <label className="btn btn-outline btn-sm file-btn">
              Add photo
              <input ref={fileRef} type="file" accept="image/jpeg,image/png,image/webp,image/gif" onChange={pickPhoto} hidden />
            </label>
            <select value={postDept} onChange={(e) => setPostDept(e.target.value)} aria-label="Post to department">
              {meta.departments.map((d) => <option key={d.code} value={d.code}>{d.label}</option>)}
            </select>
            {canNotice && (
              <select value={type} onChange={(e) => setType(e.target.value)} aria-label="Post type">
                <option value="POST">Regular post</option>
                <option value="NOTICE">Official notice</option>
              </select>
            )}
            <button className="btn btn-primary push-right" disabled={posting || !content.trim()}>
              {posting ? 'Posting…' : 'Post'}
            </button>
          </div>
        </form>
      </section>

      <div className="chips" role="tablist" aria-label="Department feed">
        <button className={dept === 'ALL' ? 'chip active' : 'chip'} onClick={() => setDept('ALL')}>All</button>
        {meta.departments.filter((d) => d.code !== 'ALL').map((d) => (
          <button key={d.code} className={dept === d.code ? 'chip active' : 'chip'} onClick={() => setDept(d.code)}>
            {d.code}
          </button>
        ))}
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {loading && <div className="center-note">Loading feed…</div>}
      {!loading && posts.length === 0 && !error && (
        <div className="card empty">Nothing here yet. Be the first to post!</div>
      )}
      {posts.map((p) => (
        <PostCard
          key={p.id}
          post={p}
          deptLabel={p.department === 'ALL' ? 'Everyone' : p.department}
          canDelete={p.authorId === user.id || user.role === 'ADMIN'}
          onDelete={remove}
        />
      ))}
    </div>
  );
}
