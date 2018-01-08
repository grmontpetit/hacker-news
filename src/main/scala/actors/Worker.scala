package actors

import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import model.Story

import scala.concurrent.Future
import scala.language.postfixOps
import model.JsonSupport._

class Worker extends Actor {

  val config = ConfigFactory.load()
  val itemUrl = config.getString("hackernews.item")
  implicit val materializer = ActorMaterializer()
  implicit val system = core.Boot.system
  implicit val ec =  scala.concurrent.ExecutionContext.Implicits.global

  def receive = {
    case Work(storyId) => {
      val url = itemUrl.replace("$id", storyId.toString)
      val master = sender
      val future: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = url))
      future.flatMap(response => Unmarshal(response.entity).to[Story]).foreach { story =>
        master ! Reply(story)
      }
    }
  }
}
