name := "illithid"

version := "0.1"

scalaVersion := "2.12.10"

resolvers += "confluent" at "https://packages.confluent.io/maven/"

libraryDependencies ++= Seq(
  "org.jsoup" % "jsoup" % "1.13.1",
  "org.slf4j" % "slf4j-nop" % "1.7.30",
  "org.apache.flink" %% "flink-clients" % "1.12.0" % Provided,
  "org.apache.flink" %% "flink-streaming-scala" % "1.12.0" % Provided,
  "org.apache.flink" %% "flink-connector-kafka" % "1.12.0",
  "org.apache.flink" %% "flink-connector-rabbitmq" % "1.12.0",
  "org.apache.flink" % "flink-avro" % "1.12.1",
  "io.confluent" % "kafka-schema-registry-client" % "5.5.2",
  "org.apache.flink" % "flink-avro-confluent-registry" % "1.12.0",
  "com.sksamuel.avro4s" % "avro4s-kafka_2.12" % "4.0.4"
)
