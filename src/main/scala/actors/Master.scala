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
          // take x max stories out of 500
          val stories: Future[List[Story]] = Future.sequence(ids.take(maxStories).map { id =>
            (router ? GetStory(id)).flatMap {
              case StoryReply(story) => Future.successful(story)
            }})
          val intermediateFuture: Future[Future[List[CommentReply]]] = stories.map { storyList =>
              Future.sequence(storyList.map { story =>
                eval(List.empty[CommentReply], story, story.kids)
            }).map(_.flatten)
          }
          val commentsReply: Future[List[CommentReply]] = intermediateFuture.flatMap(identity)
          commentsReply.foreach { c =>
            boot ! WorkComplete(c.distinct)
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

  /**
    * Recursive function to fetch kids of kids.
    * @param acc The accumulator of the kids.
    * @param story The initial story.
    * @param kids The kids as an Option
    * @return A Future of List[CommentReply]
    */
  private def eval(acc: List[CommentReply], story: Story, kids: Option[List[Int]]): Future[List[CommentReply]] = {
    if (kids.isDefined) {
      Future.sequence(kids.get.map { kid =>
        val commentFuture: Future[CommentReply] = (router ? GetComment(story, kid)).mapTo[CommentReply]
        commentFuture.flatMap { commentReply =>
          eval(commentReply :: acc, story, commentReply.comment.kids)
        }
      }).map(_.flatten)
    } else {
      Future.successful(acc)
    }
  }
}