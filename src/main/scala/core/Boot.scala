package core

import akka.pattern.ask
import actors._
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
      val allComments = commentsReplies.map(_.comment)
      commentsReplies.groupBy(_.story).map(d => d._1 -> d._2.map(_.comment)).foreach(item => printLine(item, allComments))

      val stop = dispatcher ? StopWork
      stop.onComplete {
        case Success(_) => system.terminate()
        case _          => system.terminate() // stop anyways
      }
    case Failure(reason) => println("Failure: " + reason)
  }

  /**
    * Print story informations to console.
    * @param story The story is a tuple of story and the list of comments, including all childs.
    * @param allComments All the comments for all the stories.
    */
  private def printLine(story: (Story, List[Comment]), allComments: List[Comment]): Unit = {
    val topTen = story._2.filter(_.by.isDefined).map(_.by.get).groupBy(identity).mapValues(_.size).toSeq.sortWith(_._2 > _._2).take(10)
    //println(topTen)
    println(s"| ${story._1.title} | ${topTen.map(t => t._1 + " (" + t._2 + " for story - " + allComments.count(f => f.by.getOrElse("") == t._1) + " total) | ").mkString}")
  }

}
