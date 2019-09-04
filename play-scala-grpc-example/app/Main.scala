import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import akka.stream.ActorMaterializer
import example.myapp.helloworld.grpc.{GreeterService, GreeterServiceClient, HelloRequest}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Main  extends App {

  implicit val sys: ActorSystem       = ActorSystem()
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val ec:  ExecutionContext  = sys.dispatcher

  val httpClietSettings: HttpClientSettings = HttpClientSettings()
  val httpClient: GreeterHttpClient = new GreeterHttpClient(httpClietSettings)

  val grpcClientSettings = GrpcClientSettings.fromConfig(GreeterService.name)
  val grpcClient: GreeterServiceClient = GreeterServiceClient(grpcClientSettings)

  sayHelloByHttp()
  sayHelloByGrpc()

  def sayHelloByHttp(): Unit = {
    sys.log.info("Performing request")
    val reply = httpClient.sayHello("Bob")
    reply.onComplete {
      case Success(name) =>
        println(s"sayHelloByHttp reply: $name")
      case Failure(e) =>
        println(s"Error sayHelloByHttp: $e")
    }
  }

  def sayHelloByGrpc(): Unit = {
    sys.log.info("Performing request")
    val reply = grpcClient.sayHello(HelloRequest("Bob"))
    reply.onComplete {
      case Success(name) =>
        println(s"sayHelloByGrpc reply: $name")
      case Failure(e) =>
        println(s"Error sayHelloByGrpc: $e")
    }
  }
}
