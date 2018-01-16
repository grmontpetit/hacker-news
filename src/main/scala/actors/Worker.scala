package actors

import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import model.JsonSupport._
import model.{Comment, Story}

import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.concurrent.duration.DurationInt

class Worker extends Actor {

  implicit val system = core.Boot.system
  implicit val materializer = ActorMaterializer()
  implicit val ec =  scala.concurrent.ExecutionContext.Implicits.global

  val config = ConfigFactory.load()
  val itemUrl = config.getString("hackernews.item")

  type URL = Int => String

  def receive = {
    case GetStory(id) =>
      val master = sender
      val story: Future[Story] = exec(id).flatMap { response =>
        Unmarshal(response.entity).to[Story]
      }
      Await.result(story, 5 seconds) match {
        case story: Story => master ! StoryReply(story)
      }

    case GetComments(story, item) => {
      val master = sender
      val comment: Future[Comment] = exec(item).flatMap { response =>
        Unmarshal(response.entity).to[Comment]
      }
      Await.result(comment, 5 seconds) match {
        case comment: Comment => master ! CommentsReply(story, comment)
      }
    }
  }

  private val url: URL = { id =>
    itemUrl.replace("$id", id.toString)
  }

  private def exec: Int => Future[HttpResponse] = { id =>
    println(s"exec $id")
    Http().singleRequest(HttpRequest(uri = url.apply(id)))
  }

}
