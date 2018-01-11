package actors

import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import model.{Comment, Story}

import scala.concurrent.Future
import scala.language.postfixOps
import model.JsonSupport._

class Worker extends Actor {

  val config = ConfigFactory.load()
  val itemUrl = config.getString("hackernews.item")
  implicit val materializer = ActorMaterializer()
  implicit val system = core.Boot.system
  implicit val ec =  scala.concurrent.ExecutionContext.Implicits.global

  type URL = Int => String

  def receive = {
    case Work(storyId) => {
      val master = sender
      val future: Future[HttpResponse] = exec(storyId)
      future.flatMap(response => Unmarshal(response.entity).to[Story]).foreach { story =>
        val futureComments: Future[List[Comment]] = Future.sequence(story.kids.map(c => exec(c).flatMap(r => Unmarshal(r.entity).to[Comment])))
        
        master ! Reply(story)
      }
    }
  }

  private val url: URL = { id =>
    itemUrl.replace("$id", id.toString)
  }

  private def exec: Int => Future[HttpResponse] = { id =>
    Http().singleRequest(HttpRequest(uri = url.apply(id)))
  }

}
