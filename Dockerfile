FROM gradle:8.7.0-jdk17-alpine

RUN apk update && apk add --no-cache curl python3
ADD "https://api.github.com/repos/yt-dlp/yt-dlp/releases?per_page=1" latest_release
RUN curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp -o /usr/local/bin/yt-dlp \
    && chmod a+rx /usr/local/bin/yt-dlp

WORKDIR /app
COPY . .
ENTRYPOINT ["gradle", "bootRun"]