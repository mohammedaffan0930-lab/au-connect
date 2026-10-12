/** The AU Connect logo: an "AU" monogram on a navy tile with an amber "connection" arc. */
export default function Logo({ size = 40 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 64 64" role="img" aria-label="AU Connect logo">
      <defs>
        <linearGradient id="aug" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stopColor="#2a3fa8" />
          <stop offset="1" stopColor="#0e1740" />
        </linearGradient>
      </defs>
      <rect width="64" height="64" rx="15" fill="url(#aug)" />
      <g fill="none" stroke="#ffffff" strokeWidth="5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M10 44 L21 15 L32 44" />
        <path d="M14.5 34 H27.5" />
        <path d="M38 15 V32 Q38 44 46 44 Q54 44 54 32 V15" />
      </g>
      <path d="M11 53 Q32 62 53 53" fill="none" stroke="#f5a524" strokeWidth="3" strokeLinecap="round" />
    </svg>
  );
}
