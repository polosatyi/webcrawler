FROM hseeberger/scala-sbt:graalvm-ce-20.0.0-java8_1.3.13_2.12.12 as build
COPY . /extractor
WORKDIR /extractor
RUN sbt assembly
