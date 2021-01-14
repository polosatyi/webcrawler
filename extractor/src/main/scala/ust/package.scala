import java.security.MessageDigest
import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.api.common.state.{MapState, MapStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.util.Collector


package object ust {

  class URLSeenTest extends RichFlatMapFunction[String, String] {

    private var ust: MapState[String, Boolean] = _

    private def md5(s: String): String = { toHex(MessageDigest.getInstance("MD5").digest(s.getBytes("UTF-8")))}
    private def toHex(bytes: Array[Byte]): String = bytes.map("%02x".format(_)).mkString("")

    override def flatMap(url: String, out: Collector[String]): Unit = {

      val urlHash = md5(url)
      if (!ust.contains(urlHash)) {
        println(url, urlHash)
        ust.put(urlHash, true)
        out.collect(url)
      }
    }

    override def open(parameters: Configuration): Unit = {
      val descriptor = new MapStateDescriptor[String, Boolean]("ust", classOf[String], classOf[Boolean])
      ust = getRuntimeContext.getMapState(descriptor)
    }
  }

}
