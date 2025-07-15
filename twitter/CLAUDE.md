# Twitter Module

This module provides comprehensive Twitter API functionality for the TwitterX project, including tweet fetching, user timeline monitoring, and RSS feed processing.

## Module Structure

The Twitter module is organized into several sub-modules:

- **twitter:api** - Core interfaces and models for Twitter API interactions
- **twitter:fx** - FixTweet API implementation for tweet fetching
- **twitter:nitter** - Nitter RSS feed implementation for timeline monitoring
- **twitter:impl** - Combined service implementation that coordinates different providers
- **twitter:e2e** - End-to-end tests for Twitter functionality

## FxTwitter API (twitter:fx)

This is the main FixTweet API implementation that provides access to Twitter data without requiring an API key.

### Status API Endpoint

`https://api.fxtwitter.com/:screen_name?/status/:id/:translate_to?`

- `screen_name` - The screen name (@ handle) of the tweet, which is ignored
- `id` - The ID of the status (tweet)
- `translate_to?` - 2 letter ISO language code of the language you want to translate the tweet into

### Response Format

Returns a JSON object with the following structure:

```json
{
    "code": 200,
    "message": "OK",
    "tweet": {
        "url": "https://twitter.com/dangeredwolf/status/1548602399862013953",
        "text": "I made my first ever TikTok....",
        "created_at": "Sun Jul 17 09:35:58 +0000 2022",
        "created_timestamp": 1658050558,
        "author": {
            "name": "dangered wolf",
            "screen_name": "dangeredwolf",
            "avatar_url": "https://pbs.twimg.com/profile_images/1532100022648680450/2z6Ml6Qy_200x200.jpg",
            "avatar_color": "#3487b2",
            "banner_url": "https://pbs.twimg.com/profile_banners/3784131322/1658599775"
        },
        "replies": 9,
        "retweets": 3,
        "likes": 46,
        "views": 342,
        "color": "#0a7c2f",
        "twitter_card": "player",
        "lang": "en",
        "source": "Twitter for iPhone",
        "replying_to": null,
        "replying_to_status": null,
        "media": {
            "videos": [
                {
                    "url": "https://video.twimg.com/ext_tw_video/1548602342488129536/pu/vid/720x1280/I_D3svYfjBl7_xGS.mp4?tag=14",
                    "thumbnail_url": "https://pbs.twimg.com/ext_tw_video_thumb/1548602342488129536/pu/img/V_1u5Nv5BwKBynwv.jpg",
                    "width": 720,
                    "height": 1280,
                    "duration": 25.133,
                    "format": "video/mp4",
                    "type": "video"
                }
            ]
        }
    }
}
```

### Response Codes

- 200 (OK) - Success
- 401 (PRIVATE_TWEET) - Tweet is private
- 404 (NOT_FOUND) - Tweet not found
- 500 (API_FAIL) - API failure

### API Models

#### APITweet

Core tweet information container with:

**Core attributes:**
- `id` - Tweet ID
- `url` - Link to original tweet
- `text` - Tweet content
- `created_at` - Creation timestamp
- `lang` - Detected language
- `author` - Tweet author information
- `source` - Tweet source application

**Interaction counts:**
- `likes` - Like count
- `retweets` - Retweet count
- `replies` - Reply count
- `views` - View count (may be null)

**Media and embeds:**
- `media` - Photos, videos, external media
- `quote` - Quoted tweet (if applicable)
- `poll` - Poll data (if applicable)
- `translation` - Translation results (if requested)

#### APIAuthor

User information:
- `name` - Display name
- `screen_name` - Username handle
- `avatar_url` - Profile picture URL
- `avatar_color` - Profile color
- `banner_url` - Banner image URL

#### Media Types

- **APIPhoto** - Photo data with dimensions and URL
- **APIVideo** - Video data with format, duration, and thumbnail
- **APIMosaicPhoto** - Stitched photo collections
- **APIExternalMedia** - External media embeds

## Nitter RSS Integration (twitter:nitter)

Nitter generates RSS 2.0 XML feeds for Twitter content monitoring.

**Local Nitter Instance:** `http://127.0.0.1:8049`

### RSS Feed Types

- **User Timeline RSS** (`/@name/rss`) - Feeds for user timelines

### RSS XML Structure

The RSS feeds include:
- Tweet text with URL replacements
- Media content (images, videos, GIFs) as HTML elements
- Quote tweet links
- Card previews when available
- Dublin Core namespace for creator information
- RSS feed metadata including title, description, and TTL
- RSS item metadata including title, description, publication date, and links

### RSS Caching Strategy

RSS feeds are cached in Redis using a hash-based approach:
- The cursor (pagination token) is stored under the "min" key
- The compressed RSS content is stored under the "rss" key
- Cache expiration is controlled by `rssCacheTime` configuration

## Implementation Module (twitter:impl)

Combined service implementation that coordinates different Twitter providers and implements the `TwitterService` interface from the `twitter:api` module.

## End-to-End Testing (twitter:e2e)

Comprehensive test suite for Twitter functionality validation.

### Testing Commands

- `curl https://api.fxtwitter.com/status/TWEET_ID` - Test tweet fetching
- Use `FxTwitterService.kt` for tweet-by-ID operations
- Use `NitterService.kt` for username-based operations and timeline monitoring
- Use `TwitterServiceImpl.kt` for combined functionality

### Test Cases

The E2E module covers:

- **Single Tweet Types:**
  - Text-only tweets
  - Image tweets (single and multiple)
  - Video tweets
  - GIF tweets

- **Tweet Interactions:**
  - Reply chains
  - Quote tweets
  - Retweets
  - Private tweets

- **User Operations:**
  - Getting latest tweets by username
  - User timeline monitoring

### Example Test URLs
[]: Single tweet with only text by link https://x.com/heydave7/status/1942258118710476992

[]: Single tweet with only image by link https://x.com/babaikit/status/1942228890996408429
[]: Single tweet with text and image by link https://x.com/grntmedia/status/1942300094210117808
[]: Single tweet with multiply image by link https://x.com/TheAppleDesign/status/1942251733066891494

[]: Single tweet with only video by link https://x.com/NoContextCrap/status/1942262836660465946
[]: Single tweet with text and video by link https://x.com/Fodorpalaezepa/status/1942304182897185056

[]: Single tweet with text and gif by link https://x.com/Revv180/status/1942267259541487927

[]: Replies with tweet by link https://x.com/elonmusk/status/1942120888771752133
- Parent tweet by link https://x.com/AutismCapital/status/1942120394594615617
- Parent tweet with image by link https://x.com/elonmusk/status/1942119635341754538

[]: Quoted with tweet by link https://x.com/elonmusk/status/1942128989239459865
- Original tweet by link https://x.com/beinlibertarian/status/1941880791908200933

[]: Retweet with tweet by link https://x.com/elonmusk/status/1942273130980073624
- Original tweet by link https://x.com/techdevnotes/status/1942273130980073624 and author by link https://x.com/techdevnotes

[]: Private tweet by link

[]: Get last tweet ids by username 'elonmusk'


## Testing

### Unit Tests
```bash
./gradlew :twitter:fx:test
./gradlew :twitter:nitter:test
./gradlew :twitter:impl:test
```

### End-to-End Tests
```bash
./gradlew :twitter:e2e:test
```
