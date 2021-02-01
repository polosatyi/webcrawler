import java.security.MessageDigest

import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.api.common.state.{MapState, MapStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.util.Collector


package object ust {

  class URLSeenTest extends RichFlatMapFunction[(String, String), (String, String)] {

    private var ust: MapState[String, Boolean] = _
    private def urlBone(url: String): String = {
      for (prefix <- Array("https://www.", "https://", "http://www.", "http://")) {
        if (url.toLowerCase().startsWith(prefix)) {
          url.drop(prefix.length)
        }
      }
      if (url.endsWith("/")) {
        url.dropRight(1)
      }
      url
    }
    private def md5(s: String): String = {
      toHex(MessageDigest.getInstance("MD5").digest(s.getBytes("UTF-8")))
    }
    private def toHex(bytes: Array[Byte]): String = bytes.map("%02x".format(_)).mkString("")

    override def flatMap(item: (String, String), out: Collector[(String, String)]): Unit = {
      val url = item._2
      if (url == "CLEAR") {
        ust.clear()
        val domain = item._1
        println(f"CLEARing the state for $domain")
      } else {
        val urlHash = md5(urlBone(url))
        if (!ust.contains(urlHash)) {
          println(url, urlHash)
          ust.put(urlHash, true)
          out.collect(item)
        }
      }
    }

    override def open(parameters: Configuration): Unit = {
      val descriptor = new MapStateDescriptor[String, Boolean]("ust", classOf[String], classOf[Boolean])
      ust = getRuntimeContext.getMapState(descriptor)
    }
  }

}
