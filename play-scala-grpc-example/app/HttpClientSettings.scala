import akka.http.scaladsl.model.Uri

final case class HttpClientSettings(
  scheme: String = "https",
  host: String = "localhost",
  port: Int = 9443,
  queueSize: Int = 10,
  https: Boolean = false
) {
  def urlString: String = s"$scheme://$host$port"
  def uri: Uri          = Uri.from(scheme = scheme, host = host, port = port)
}
