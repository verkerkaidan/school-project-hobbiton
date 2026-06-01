import protobuf from "protobufjs";

const CHAT_PROTO = `
syntax = "proto3";

message ChatMessage {
  string text = 1;
  string userId = 2;
}
`;

let ChatMessage: protobuf.Type;

function getChatMessage(): protobuf.Type {
  if (ChatMessage) return ChatMessage;

  const root = protobuf.parse(CHAT_PROTO).root;

  ChatMessage = root.lookupType("ChatMessage");

  return ChatMessage;
}

export function encodeMessage(text: string, userId: string): Uint8Array {
  const type = getChatMessage();
  const message = type.create({ text, userId });
  return type.encode(message).finish() as Uint8Array;
}

export function decodeMessage(buffer: ArrayBuffer): { text: string; userId: string } {
  const type = getChatMessage();
  const bytes = new Uint8Array(buffer);
  const decoded = type.decode(new Uint8Array(buffer)) as unknown as { text: string; userId: string };
  return { text: decoded.text, userId: decoded.userId };
}