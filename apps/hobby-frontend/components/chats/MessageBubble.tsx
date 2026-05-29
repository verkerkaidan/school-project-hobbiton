export default function MessageBubble({
  message,
  ownId,
}: {
  message: { text: string; userId: string };
  ownId: string;
}) {
  const own = message.userId === ownId;

  return (
    <div className={`max-w-[75%] rounded-3xl px-4 py-3 ${
      own ? "ml-auto bg-brand text-white" : "mr-auto bg-zinc-800 text-zinc-100"
    }`}>
      {!own && (
        <p className="mb-1 text-xs text-zinc-400">{message.userId}</p>
      )}
      {message.text}
    </div>
  );
}