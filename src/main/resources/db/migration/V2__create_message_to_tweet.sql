CREATE TABLE IF NOT EXISTS message_to_tweet (
    message_id BIGINT PRIMARY KEY NOT NULL,
    tweet_id BIGINT NOT NULL
);