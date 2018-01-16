package actors

import akka.actor.{Actor, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling._
import akka.pattern.ask
import akka.routing.RoundRobinPool
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import model.JsonSupport._
import model.Story

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class Master extends Actor {

  val config = ConfigFactory.load()
  val topStories = config.getString("hackernews.topstories")
  val maxStories = config.getInt("hackernews.maxstories")
  val maxoutgoinghttp = config.getInt("akka.maxoutgoinghttp")
  val akkaTimeout = config.getInt("akka.timeout")

  implicit val materializer = ActorMaterializer()
  implicit val system = core.Boot.system
  implicit val timeout = Timeout(akkaTimeout seconds)

  val router = context.actorOf(Props[Worker].withRouter(RoundRobinPool(nrOfInstances = maxoutgoinghttp)), "router")

  def receive = {
    case StartWork =>
      val boot = sender
      // fetch top 500 stories
      val future: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = topStories))
      future.foreach { result =>
        Unmarshal(result.entity).to[List[Int]].foreach { ids =>
          val stories: Future[List[Story]] = Future.sequence(ids.take(maxStories).map { id =>
            (router ? GetStory(id)).flatMap {
              case StoryReply(story) => Future.successful(story)
            }})
          val intermediateFuture: Future[Future[List[CommentsReply]]] = stories.map { storyList =>
              Future.sequence(storyList.map { story =>
              Future.sequence(story.kids.getOrElse(List.empty[Int]).map { kid =>
                eval(story, kid)
              }).map(_.reduceLeft(_ ++ _))
            }).map(_.reduceLeft(_ ++ _))
          }
          val commentsReply: Future[List[CommentsReply]] = intermediateFuture.flatMap(identity)
          commentsReply.foreach { c =>
            boot ! WorkComplete(c)
          }
        }
      }

    case StopWork =>
      Http().shutdownAllConnectionPools()
      context.stop(router)
      context.stop(self)
      sender ! "done"
    case _ => Unit
  }

  private def eval(story: Story, id: Int): Future[List[CommentsReply]] = {
    val comments: Future[CommentsReply] = (router ? GetComments(story, id)).mapTo[CommentsReply]
    comments.flatMap { comment =>
      if (comment.comment.kids.isDefined) {
        Future.sequence (
          comment.comment.kids.get.map(k => eval(story, k))
          ).map(_.fold(List.empty[CommentsReply])(_ ++ _))
      }
      else Future.successful(List.empty[CommentsReply])
    }
  }
}