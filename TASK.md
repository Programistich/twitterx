# Technical Task: TwitterX

## 1. Project Goal

* **Product**: Telegram bot
* **Target Audience**: Telegram users who want to follow Twitter accounts without leaving the messenger
* **Problem**: Inconvenience of switching between Telegram and Twitter to read new tweets; suboptimal display of links; language barrier
* **Value**:

  * Subscribe to a Twitter profile
  * Receive new tweets with translation directly in chat
  * View tweets in a convenient format
  * Download videos from popular social networks

---

## 2. Functional Requirements

### 2.1. Managing Twitter Subscriptions

* **Adding**:

  * Via the `/add @username` command (validate existence via Nitter)
  * When sending a link like `https://x.com/{username}`, bot offers to subscribe
* **Removing**:

  * Via the `/remove @username` command
* **View Subscriptions**:

  * `/list` displays user's active subscriptions

### 2.2. Tweet Processing and Display

* **Automatic Delivery**: Monitors new tweets and sends them to the user
* **Link Processing**: Sending a message with only a tweet link (`.../status/{id}`) triggers formatted tweet response
* **Content Type Support**:

  * Text: Full tweet content
  * Media: Photos, videos, GIFs
  * Polls, Articles, Broadcasts: Displayed correctly
* **Tweet Chain Processing**: Replies, retweets, and quotes are expanded into full chain from original to latest use reply to previous message

### 2.3. Inline Query Mode

* Typing `@BotName <tweet_link>` allows sending formatted tweets via inline mode

### 2.4. Additional Functionality: Video Downloader

* Support for video download from TikTok, YouTube Shorts, Instagram Reels via link
* Uses `yt-dlp` library

### 2.5. Basic Commands

* `/start`: Welcome message and instructions
* `/add @username`: Add a subscription
* `/remove @username`: Remove a subscription
* `/list`: Show current subscriptions
* `/lang`: Show language selection menu

### 2.6. Tweet Translation

* `/lang` shows inline buttons:

  * 🇬🇧 English
  * 🇺🇦 Ukrainian
  * 🇷🇺 Russian
* Selected language saved per user
* Interface must support English, Ukrainian, Russian

## 2.7. Long text
* If a tweet exceeds 4096 characters, it is publishes in some sites

---

## 3. Non-Functional Requirements

* **Performance**: Check for new tweets every 5 minutes
* **Reliability**: Notify user once if Nitter is down
* **API Limits**: Respect Nitter/Twitter limits (≤ 500 requests per 15 minutes)

---

## 4. Integrations

* **Twitter Data Source**: Own Nitter instance (via RSS feeds)
* **Telegram Bot API**: Use latest stable version
* **Video Downloader (yt-dlp)**:

  * Must support cookies
  * Implement mechanism for cookie updates without code changes
* **Translation Service**: Integrate a free service (e.g., `deep-translator`)

---

## 5. Technologies

* **Language**: Kotlin + Spring Boot
* **Database**: PostgreSQL
* **Hosting**: Own server
* **Localization**:

  * Use standard i18n libraries
  * Export all interface texts to separate localization files (e.g., JSON/YAML)

---

## 6. User Stories

1. **Subscribe to new account**
   *As a user, I want to add `@username` to subscriptions to receive new tweets.*
2. **Tweet link processing**
   *As a user, I want to see the original tweet when sending a reply link.*
3. **Video upload**
   *As a user, I want to get video files from Instagram Reels.*
4. **Automatic tweet retrieval**
   *As a user, I want new tweets (including threads) to be delivered automatically.*
5. **Language settings**
   *As a user, I want to select the translation language via menu.*

---

## 7. Security Restrictions and Requirements

* **Data Privacy**: Store only Telegram User ID and subscriptions; no encryption required
* **Abuse Protection**: No rate limiting required
* **NSFW Handling**: Content sent "as is", no spoiler mechanism

---

## 8. UI/UX (Message and Button Appearance)

* **Tweet Format**:
  `Tweet from @username`
  `[UA] Translated tweet text...`
  `[EN] Original tweet text...`
  Media sent separately

* **/list Command Output**:
  Simple text list of subscriptions

* **Language Selection Menu** (`/lang`):

  * 🇬🇧 English
  * 🇺🇦 Ukrainian
  * 🇷🇺 Russian

* **Interface Localization Examples**:

  * `/list`:

    * UA: `Ваші підписки:`
    * EN: `Your subscriptions:`
    * RU: `Ваши подписки:`

  * `/start`:

    * UA: `Привіт! Я бот, який допоможе вам читати Twitter...`
    * EN: `Hello! I am a bot that will help you read Twitter...`
    * RU: `Привет! Я бот, который поможет вам читать Twitter...`

---

## 9. Architectural Approaches and Patterns

* **SOLID**: Follow all five principles for maintainability
* **GRASP**: Apply patterns for Low Coupling, High Cohesion
* **Chain of Responsibility**: Each handler checks Telegram updates and processes if applicable

---

## 10. Team


* Single developer project

---
