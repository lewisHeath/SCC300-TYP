FROM maven:3-openjdk-17

ARG user=appuser
ARG group=appuser
ARG uid=1000
ARG gid=1000

RUN groupadd -g ${gid} ${group} && useradd -u ${uid} -g ${gid} ${user}

WORKDIR /app

COPY pom.xml ./
RUN mvn package

# Copy the rest of the application code into the container
COPY src/main/java/jabs ./src

CMD ["sh"]

