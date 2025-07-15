FROM gradle:8.7-jdk17 AS build
WORKDIR /app
COPY . .
ENV SPRING_PROFILES_ACTIVE production
RUN gradle clean :app:bootJar

FROM eclipse-temurin:17-jdk
WORKDIR /app

COPY --from=build /app/app/build/libs/*.jar app.jar

RUN apt-get update && apt-get upgrade -y && \
    apt-get install -y curl python3 bash

ADD "https://api.github.com/repos/yt-dlp/yt-dlp/releases?per_page=1" latest_release
RUN curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp -o /usr/local/bin/yt-dlp && \
    chmod a+rx /usr/local/bin/yt-dlp

ENTRYPOINT ["java", "-jar", "app.jar"]
