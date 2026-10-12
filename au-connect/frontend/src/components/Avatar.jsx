export default function Avatar({ name = '?', url, size = 40 }) {
  const initials = name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((w) => w[0].toUpperCase())
    .join('');
  const style = { width: size, height: size, fontSize: size * 0.38 };
  return url ? (
    <img className="avatar" src={url} alt={name} style={style} />
  ) : (
    <span className="avatar avatar-initials" style={style} aria-label={name}>
      {initials || '?'}
    </span>
  );
}
