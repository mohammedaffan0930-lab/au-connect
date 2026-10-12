import { Link } from 'react-router-dom';
import Avatar from './Avatar.jsx';
import { timeAgo } from '../api.js';

export default function PostCard({ post, canDelete, onDelete, deptLabel }) {
  const isNotice = post.type === 'NOTICE';
  return (
    <article className={isNotice ? 'card post notice' : 'card post'}>
      <header className="post-head">
        <Link to={`/profile/${post.authorId}`}>
          <Avatar name={post.authorName} url={post.authorPhotoUrl} size={42} />
        </Link>
        <div className="post-meta">
          <Link to={`/profile/${post.authorId}`} className="post-author">{post.authorName}</Link>
          <div className="muted small">
            {post.authorRole !== 'STUDENT' && <span className="badge badge-role">{post.authorRole}</span>}
            <span className="badge">{deptLabel}</span>
            <span>{timeAgo(post.createdAt)}</span>
          </div>
        </div>
        {isNotice && <span className="badge badge-notice">Official notice</span>}
        {canDelete && (
          <button className="btn btn-ghost btn-sm" onClick={() => onDelete(post.id)}>Delete</button>
        )}
      </header>
      <p className="post-body">{post.content}</p>
      {post.photoUrl && <img className="post-photo" src={post.photoUrl} alt="Shared with this post" loading="lazy" />}
    </article>
  );
}
