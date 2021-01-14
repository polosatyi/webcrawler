import java.util.Properties
import scala.collection.JavaConverters._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.streaming.connectors.rabbitmq.RMQSink
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig
import utils._
import ust.URLSeenTest


object Extractor extends App {

  override def main(args: Array[String]): Unit = {

    if (args.length != 3) {
      throw new Exception(
        "You must pass 3 arguments: {domain} {kafkaTopic} {rmqQueue}"
      )
    }

    val (domain, kafkaTopic, rmqQueue) = (args(0), args(1), args(2))

    println("starting...")

    // Kafka settings
    val kafkaBroker01: String = sys.env.getOrElse("KAFKA_BROKER_O1", "kafka:29092")
    val kafkaGroupId: String = "extractors"

    // RMQ settings
    val rmqHost: String = sys.env.getOrElse("RABBITMQ_HOST", "rmq")
    val rmqPort: Int = sys.env.getOrElse("RABBITMQ_PORT", "5672").toInt
    val rmqUser: String = sys.env.getOrElse("RABBITMQ_USER", "rmq")
    val rmqPassword: String = sys.env.getOrElse("RABBITMQ_PASSWORD", "rmq123")
    val rmqVhost: String = sys.env.getOrElse("RABBITMQ_VHOST", "/")

    val https = true
    val www = false
    val trailingSlash = true

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val properties = new Properties()
    properties.setProperty("bootstrap.servers", kafkaBroker01)
    properties.setProperty("group.id", kafkaGroupId)
    val stream = env.addSource(new FlinkKafkaConsumer[String](
      kafkaTopic, new SimpleStringSchema(), properties))

    val rmqConnectionConfig = new RMQConnectionConfig.Builder()
      .setHost(rmqHost)
      .setPort(rmqPort)
      .setUserName(rmqUser)
      .setPassword(rmqPassword)
      .setVirtualHost(rmqVhost)
      .build()

    stream
      .flatMap(html => {
        val document: Document = Jsoup.parse(html)
        document.select("a[href]").asScala
          .map(element => element.attr("href") )
          .filterNot(_.startsWith("#"))
          .filterNot(_.toLowerCase().endsWith(".jpeg"))
          .filterNot(_.toLowerCase().endsWith(".jpg"))
          .filterNot(_.toLowerCase().endsWith(".png"))
          .filterNot(_.toLowerCase().endsWith(".gif"))
          .map(url => formatRelativeUrl(domain, url))
          .map(url => removeFragments(url))
          .filter(isFromSite(domain, _))
          .map(url => formatUrlScheme(url, https))
          .map(url => formatWwwPrefix(domain, url, www))
          .map(url => formatTrailingSlash(url, trailingSlash))
          .distinct
      })
      .keyBy(r => "dummyKey")
      .flatMap(new URLSeenTest())
      .addSink(new RMQSink[String](rmqConnectionConfig, rmqQueue, new SimpleStringSchema))

    env.execute("Process HTML files")
  }
}
