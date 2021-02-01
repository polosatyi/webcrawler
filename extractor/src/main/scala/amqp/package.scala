import com.rabbitmq.client.AMQP
import org.apache.flink.api.common.serialization.SerializationSchema
import org.apache.flink.streaming.connectors.rabbitmq.RMQSinkPublishOptions


package object amqp {
  class MessageSchema extends SerializationSchema[(String, String)] {

    override def serialize(element: (String, String)): Array[Byte] = (element match {
      case (_, u) => u
    }).getBytes
  }

  object MessageSchema {
    def apply() = new MessageSchema()
  }

  object MessagePublishOptions extends RMQSinkPublishOptions[(String, String)] {
    override def computeRoutingKey(a: (String, String)): String = a match {
      case (q, _) => "frontier"
    }

    override def computeProperties(a: (String, String)): AMQP.BasicProperties = null

    override def computeExchange(a: (String, String)): String = ""
  }
}
