package core

import actors.{Master, StartWork}
import akka.actor.{ActorSystem, Props}
import akka.util.Timeout

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object Boot extends App {

  println("Application started")

  implicit val system = ActorSystem("Routing")

  implicit val timeout = Timeout(3 seconds)

  val dispatcher = system.actorOf(Props[Master], name = "master")

  // Ask the master to start working
  dispatcher ! StartWork

}
