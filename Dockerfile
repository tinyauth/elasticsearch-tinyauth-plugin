
FROM openjdk:8-jdk AS builder

ENV GRADLE_VERSION 3.3
ENV GRADLE_SHA c58650c278d8cf0696cab65108ae3c8d95eea9c1938e0eb8b997095d5ca9a292

RUN cd /usr/lib \
 && curl -fl https://downloads.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -o gradle-bin.zip \
 && echo "$GRADLE_SHA gradle-bin.zip" | sha256sum -c - \
 && unzip "gradle-bin.zip" \
 && ln -s "/usr/lib/gradle-${GRADLE_VERSION}/bin/gradle" /usr/bin/gradle \
 && rm "gradle-bin.zip"

# Set Appropriate Environmental Variables
ENV GRADLE_HOME /usr/lib/gradle
ENV PATH $PATH:$GRADLE_HOME/bin

COPY build.gradle /src/build.gradle
WORKDIR /src
RUN gradle downloadDependencies

COPY . .
RUN gradle assemble

FROM elasticsearch:5.5.2

COPY elasticsearch.yml /usr/share/elasticsearch/config/elasticsearch.yml
COPY --from=builder /src/build/distributions/tinyauth-0.0.1-SNAPSHOT.zip /
RUN elasticsearch-plugin install file:/tinyauth-0.0.1-SNAPSHOT.zip
