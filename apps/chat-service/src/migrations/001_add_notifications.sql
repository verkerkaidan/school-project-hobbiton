ALTER TABLE chat_item ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT NOW();
CREATE INDEX IF NOT EXISTS idx_chat_item_chat_created ON chat_item(chat_id, created_at);

ALTER TABLE chat_item ADD COLUMN IF NOT EXISTS user_id UUID;

CREATE TABLE IF NOT EXISTS user_chat_state (
    user_id UUID NOT NULL,
    chat_id UUID NOT NULL,
    last_read_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, chat_id)
);