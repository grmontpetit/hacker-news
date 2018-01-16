package core

import akka.pattern.ask
import actors.{Master, StartWork, StopWork, WorkComplete}
import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import model.{Comment, Story}

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Failure, Success}

object Boot extends App {

  println("Application started")
  val config = ConfigFactory.load()
  val maxstories = config.getInt("hackernews.maxstories")
  val akkaTimeout = config.getInt("akka.timeout")

  implicit val system = ActorSystem("hacker-news")
  implicit val timeout = Timeout(akkaTimeout seconds)
  implicit val ec =  scala.concurrent.ExecutionContext.Implicits.global

  val dispatcher = system.actorOf(Props[Master], name = "master")

  // Ask the master to start working
  val complete = dispatcher ? StartWork

  complete.onComplete {
    case Success(WorkComplete(commentsReplies)) =>
      println("success " + commentsReplies.size)
      commentsReplies.foreach(println)
      val stop = dispatcher ? StopWork
      stop.onComplete {
        case Success(_) => system.terminate()
        case _          => system.terminate() // stop anyways
      }
    case Failure(reason) => println("Failure: " + reason)
  }

  private def formatLine(tuple: (Story, List[Comment])): String = {
    s"| ${tuple._1.title} "
  }

}
