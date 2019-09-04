import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{ActorMaterializer, Materializer}

import scala.concurrent.{ExecutionContext, Future}

final class GreeterHttpClient(settings: HttpClientSettings)(implicit system: ActorSystem) {
  implicit val mat : ActorMaterializer = ActorMaterializer()

  private val httpClient : HttpClient = new HttpClient(settings.queueSize)

  protected def handleResponse[A](
    response : HttpResponse
  )(f : Unmarshal[HttpResponse] => Future[A])(implicit ec : ExecutionContext, mat : Materializer) : Future[A] = {
    val unmarshal = Unmarshal(response)
    if (response.status.isSuccess())
      f(unmarshal)
    else
      unmarshal.to[String].flatMap { e =>
        Future.failed(new Exception(e))
      }
  }

  def sayHello(name: String)(implicit ec: ExecutionContext) : Future[String] = {
    Source
      .single {
        HttpRequest(
          method = GET,
          uri = settings.uri.withQuery(Query("name" -> name))
        )
      }.mapAsync(1)(httpClient.sendRequest)
      .mapAsync(1) { response =>
        handleResponse(response)(_.to[String])
      }.toMat(Sink.head)(Keep.right).run()
  }
}
