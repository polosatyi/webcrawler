import java.util.Properties

import scala.collection.JavaConverters._
import scala.collection.mutable
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.formats.avro.typeutils.GenericRecordAvroTypeInfo
import org.apache.flink.formats.avro.AvroDeserializationSchema
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig
import org.apache.flink.streaming.connectors.rabbitmq.RMQSink
import com.sksamuel.avro4s.AvroSchema
import utils._
import ust.URLSeenTest
import schemas.URLResponse
import amqp._


object Extractor extends App {

  override def main(args: Array[String]): Unit = {

    if (args.length != 1) {
      throw new Exception(
        "You must pass an argument: {kafkaTopic}"
      )
    }

    val kafkaTopic = args(0)

    println("starting...")

    // Kafka settings
    val kafkaBroker01: String = sys.env.getOrElse("KAFKA_BROKER_O1", "kafka:29092")
    val kafkaGroupId: String = "extractors"

    // RMQ settings
    val rmqHost: String = sys.env.getOrElse("RABBITMQ_HOST", "localhost")
    val rmqPort: Int = sys.env.getOrElse("RABBITMQ_PORT", "5672").toInt
    val rmqUser: String = sys.env.getOrElse("RABBITMQ_USER", "rmq")
    val rmqPassword: String = sys.env.getOrElse("RABBITMQ_PASSWORD", "rmq123")
    val rmqVhost: String = sys.env.getOrElse("RABBITMQ_VHOST", "/")

    val https = true
    val www = false

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.getConfig.enableForceAvro()
    env.setParallelism(1)

    val properties = new Properties()
    properties.setProperty("bootstrap.servers", kafkaBroker01)
    properties.setProperty("group.id", kafkaGroupId)

    val schema: Schema = AvroSchema[URLResponse]
    val javaStream = env.getJavaEnv.addSource(new FlinkKafkaConsumer[GenericRecord](
      kafkaTopic, AvroDeserializationSchema.forGeneric(schema), properties), new GenericRecordAvroTypeInfo(schema))
    val stream = new DataStream[GenericRecord](javaStream)

    val rmqConnectionConfig = new RMQConnectionConfig.Builder()
      .setHost(rmqHost)
      .setPort(rmqPort)
      .setUserName(rmqUser)
      .setPassword(rmqPassword)
      .setVirtualHost(rmqVhost)
      .build()

    stream
      .flatMap(item => {
        val domain = item.get("domain").toString
        val url = item.get("url").toString
        val queue = item.get("queue").toString
        if (url == "CLEAR") {
          for (url <- Array(url)) yield (queue, url)
        } else {
          val document: Document = Jsoup.parse(item.get("html").toString)
          val urls: mutable.Buffer[String] = document.select("a[href]").asScala
            .map(element => element.attr("href") )
            .filterNot(_.startsWith("#"))
            .filterNot(_.toLowerCase().endsWith(".jpeg"))
            .filterNot(_.toLowerCase().endsWith(".jpg"))
            .filterNot(_.toLowerCase().endsWith(".png"))
            .filterNot(_.toLowerCase().endsWith(".gif"))
            .filterNot(_.toLowerCase().startsWith("mailto"))
            .filter(element => isASCII(element))
            .map(url => formatRelativeUrl(domain, url))
            .map(url => removeFragments(url))
            .filter(isFromSite(domain, _))
            .map(url => formatUrlScheme(url, https))
            .map(url => formatWwwPrefix(domain, url, www))
            .distinct
          urls.append(url)
          urls.distinct
          for (url <- urls) yield (domain, url)
        }
      })
      .keyBy(_._1)  // using domain as a key
      .flatMap(new URLSeenTest())
      .addSink(new RMQSink[(String, String)](rmqConnectionConfig, new MessageSchema(), MessagePublishOptions))

    // execute application
    env.execute("Process HTML files")
  }
}
