FROM gradle:8.7.0-jdk17-alpine AS build
WORKDIR /app
COPY . .
RUN gradle clean bootJar

FROM openjdk:17-alpine
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

RUN apk add --no-cache curl python3
ADD "https://api.github.com/repos/yt-dlp/yt-dlp/releases?per_page=1" latest_release
RUN curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp \
    -o /usr/local/bin/yt-dlp && chmod a+rx /usr/local/bin/yt-dlp \

ENTRYPOINT ["java", "-jar", "app.jar"]
