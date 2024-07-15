FROM gradle:8.7.0-jdk17-alpine
COPY --chown=gradle:gradle . /app
WORKDIR /app
ENTRYPOINT ["gradle", "bootRun"]