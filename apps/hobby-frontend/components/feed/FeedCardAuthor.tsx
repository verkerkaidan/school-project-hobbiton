import Link from "next/link";

interface FeedCardAuthorProps {
  username: string;
}

export function FeedCardAuthor({ username }: FeedCardAuthorProps) {
  if (!username) return null;

  return (
    <div className="flex items-center gap-2 mb-3">
      <Link
        href={`/profile/${username}`}
        onClick={(e) => e.stopPropagation()}
        className="text-xs font-medium text-accent hover:text-brand-light transition-colors duration-200"
      >
        @{username}
      </Link>
    </div>
  );
}
