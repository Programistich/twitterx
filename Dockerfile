FROM gradle:8.7.0-jdk17-slim

RUN apt update && apt install -y curl python3
ADD "https://api.github.com/repos/yt-dlp/yt-dlp/releases?per_page=1" latest_release
RUN curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp -o /usr/local/bin/yt-dlp
RUN chmod a+rx /usr/local/bin/yt-dlp

COPY --chown=gradle:gradle . /app
WORKDIR /app
ENTRYPOINT ["gradle", "bootRun"]