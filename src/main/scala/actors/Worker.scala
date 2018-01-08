package actors

import akka.actor.Actor
import model.Item

import scala.language.postfixOps
import scala.util.Random

class Worker extends Actor {

  def receive = {
    case Work(storyId) => {
      println(s"Fetching storyId $storyId ")
      val time = randomTime
      doWork(time)
      sender ! Reply(storyId, time, Item("user1", 123456, List(1, 2, 3), 654321, "This is a story text", 1515346158L, "story", url = Some("www.google.ca")))
    }
  }

  private def randomTime: Int = {
    val rnd = new Random()
    1000 + rnd.nextInt((5000 - 1000) + 1)
  }

  private def doWork(time: Int) = Thread sleep time

}
