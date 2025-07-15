CREATE TABLE tweet_tracking (
    username VARCHAR(50) NOT NULL PRIMARY KEY,
    last_tweet_id VARCHAR(100) NOT NULL,
    last_checked TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Добавим индекс для активных записей
CREATE INDEX idx_tweet_tracking_active ON tweet_tracking (is_active);

-- Добавим индекс для времени последней проверки
CREATE INDEX idx_tweet_tracking_last_checked ON tweet_tracking (last_checked);

-- Вставим запись для отслеживания Элона Маска
INSERT INTO tweet_tracking (username, last_tweet_id, last_checked, is_active)
VALUES ('elonmusk', '', CURRENT_TIMESTAMP, TRUE);