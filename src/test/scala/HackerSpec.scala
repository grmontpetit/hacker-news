
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActors, TestKit}
import akka.util.Timeout
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration.DurationInt

class HackerSpec extends TestKit(ActorSystem("HackerSpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  implicit val timeout = Timeout(5 seconds)

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "An Echo actor" must {

    "send back messages unchanged" in {
      val echo = system.actorOf(TestActors.echoActorProps)
      echo ! "echo"
      expectMsg("echo")
    }
  }
}
