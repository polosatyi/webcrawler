package object schemas {

  case class URLResponse(
    status: Int,
    domain: String,
    url: String,
    queue: String,
    html: String
  )

}
