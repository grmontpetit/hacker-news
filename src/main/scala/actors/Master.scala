package actors

import akka.pattern.ask
import akka.actor.{Actor, Props}
import akka.routing.RoundRobinPool
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import scala.concurrent.ExecutionContext.Implicits.global

class Master extends Actor {

  implicit val timeout = Timeout(10 seconds)

  val router = context.actorOf(Props[Worker].withRouter(RoundRobinPool(nrOfInstances = 5)), "router")
  val config = ConfigFactory.load()
  val topStories = config.getString("hackernews.topstories")
  val maxstories = config.getInt("hackernews.maxstories")

  def receive = {
    case StartWork =>
      // Fetch top 500 stories
      println("Fetching top 500 stories...")
      // val response: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = topStories))
      // dispatch story data and comment to workers
      (1 to maxstories).map(i => router ? Work(i)).foreach { future =>
        future.foreach {
          case reply: Reply => println(reply)
        }
      }
    case _ => Unit
  }
}
