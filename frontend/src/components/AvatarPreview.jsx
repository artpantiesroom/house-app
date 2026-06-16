import { useEffect, useState } from 'react';
import { UserCircle } from 'lucide-react';
import { residentsApi } from '../api/residentsApi.js';

export default function AvatarPreview({ avatarUrl, name, sizeClass = 'h-20 w-20', roundedClass = 'rounded-2xl' }) {
  const [objectUrl, setObjectUrl] = useState('');

  useEffect(() => {
    let active = true;
    let nextObjectUrl = '';
    if (!avatarUrl) {
      setObjectUrl('');
      return undefined;
    }
    residentsApi.fetchAvatar(avatarUrl)
      .then((blob) => {
        if (!active) return;
        nextObjectUrl = URL.createObjectURL(blob);
        setObjectUrl(nextObjectUrl);
      })
      .catch(() => {
        if (active) setObjectUrl('');
      });
    return () => {
      active = false;
      if (nextObjectUrl) URL.revokeObjectURL(nextObjectUrl);
    };
  }, [avatarUrl]);

  return (
    <div className={`grid shrink-0 place-items-center overflow-hidden border border-sky-100/15 bg-sky-400/10 ${sizeClass} ${roundedClass}`}>
      {objectUrl ? (
        <img src={objectUrl} alt="" className={`h-full w-full object-cover ${roundedClass}`} />
      ) : (
        <div className="grid h-full w-full place-items-center">
          {initials(name) ? <span className="text-lg font-semibold text-sky-100">{initials(name)}</span> : <UserCircle size={42} className="text-sky-100/70" />}
        </div>
      )}
    </div>
  );
}

function initials(name) {
  return (name || '')
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join('');
}
