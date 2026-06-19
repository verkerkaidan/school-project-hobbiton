export default function MessageBubble({
  message,
  own,
  username,
  deleted
}: {
  message: { text: string; userId: string };
  own: boolean;
  username: string;
  deleted: boolean;
}) {
  // const own = message.userId === ownId;

return (
    <div className={`w-fit lg:max-w-[25vw] max-w-[50vw] rounded-3xl px-4 py-3 ${own ? "ml-auto bg-background-secondary text-white" : "mr-auto bg-brand-muted text-zinc-100"
      }`}>
      {!own && (
        <p className="mb-1 text-xs">{username}</p>
      )}
      {!own && (
        <p className="whitespace-pre-wrap break-words text-left">
          {deleted ? <span className="italic">Message deleted</span> : message.text}
        </p>
      )}
      {own && (
        <p className="whitespace-pre-wrap break-words text-right">
          {deleted ? <span className="italic">Message deleted</span> : message.text}
        </p>
      )}
    </div>
  );
}