export default function MessageBubble({
  message,
  own,
  username
}: {
  message: { text: string; userId: string };
  own: boolean;
  username: string
}) {
  // const own = message.userId === ownId;

  return (
    <div className={`w-fit lg:max-w-[25vw] max-w-[50vw] rounded-3xl px-4 py-3 ${own ? "ml-auto bg-background-secondary text-white" : "mr-auto bg-brand-muted text-zinc-100"
      }`}>
      {!own && (
        <p className="mb-1 text-xs text-zinc-400">{username}</p>
      )}
      {!own && (
        <p className="whitespace-pre-wrap break-words text-left">
          {message.text}
        </p>
      )}
      {own && (
        <p className="whitespace-pre-wrap break-words text-right">
          {message.text}
        </p>
      )}

    </div>
  );
}