package actors

import akka.actor.{Actor, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling._
import akka.pattern.ask
import akka.routing.RoundRobinPool
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import model.JsonSupport._


class Master extends Actor with SprayJsonSupport {

  implicit val timeout = Timeout(10 seconds)
  implicit val materializer = ActorMaterializer()
  implicit val system = core.Boot.system

  val router = context.actorOf(Props[Worker].withRouter(RoundRobinPool(nrOfInstances = 5)), "router")
  val config = ConfigFactory.load()
  val topStories = config.getString("hackernews.topstories")
  val maxstories = config.getInt("hackernews.maxstories")

  def receive = {
    case StartWork =>
      // Fetch top 500 stories
      println("Fetching top 500 stories...")
      val future: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = topStories))
      future.foreach{ result =>
        Unmarshal(result.entity).to[List[Int]].foreach { id =>
          id.take(maxstories).map(i => router ? Work(i)).foreach { future =>
            future.foreach {
              case reply: Reply => println(reply)
            }
          }
        }
      }
    case _ => Unit
  }
}