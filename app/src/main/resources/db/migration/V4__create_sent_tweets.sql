CREATE TABLE sent_tweets (
    id BIGSERIAL PRIMARY KEY,
    tweet_id VARCHAR(100) NOT NULL,
    chat_id BIGINT NOT NULL,
    message_id BIGINT NOT NULL,
    parent_tweet_id VARCHAR(100),
    thread_id VARCHAR(100),
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_main_tweet BOOLEAN NOT NULL DEFAULT FALSE
);

-- Индексы для быстрого поиска
CREATE INDEX idx_sent_tweets_tweet_chat ON sent_tweets (tweet_id, chat_id);
CREATE INDEX idx_sent_tweets_chat_parent ON sent_tweets (chat_id, parent_tweet_id);
CREATE INDEX idx_sent_tweets_chat_thread ON sent_tweets (chat_id, thread_id);
CREATE INDEX idx_sent_tweets_chat_sent_at ON sent_tweets (chat_id, sent_at);

-- Уникальный индекс для предотвращения дублирования
CREATE UNIQUE INDEX idx_sent_tweets_unique ON sent_tweets (tweet_id, chat_id);