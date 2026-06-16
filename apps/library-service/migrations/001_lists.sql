CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE lists (
                       id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       user_id     TEXT NOT NULL,
                       name        TEXT NOT NULL,
                       description TEXT,
                       created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
                       updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE list_items (
                            id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            list_id     UUID NOT NULL REFERENCES lists(id) ON DELETE CASCADE,
                            item_type   TEXT NOT NULL,   -- 'BOOK', 'MOVIE', 'EXERCISE'
                            item_id     TEXT NOT NULL,   -- ISBN, IMDb ID, exercise ID
                            position    INT NOT NULL DEFAULT 0,
                            notes       TEXT,
                            added_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
                            UNIQUE (list_id, item_id, item_type)
);

CREATE INDEX idx_lists_user_id    ON lists(user_id);
CREATE INDEX idx_list_items_list  ON list_items(list_id);