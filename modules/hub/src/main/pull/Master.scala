package lila.hub
package pull

import scala.util.Try

import akka.actor._
import api._

final class Master(workerActor: Actor) extends Actor {

  private val worker = context.actorOf(
    Props(workerActor),
    name = "worker")

  private val workToDo = collection.mutable.Queue[(Any, ActorRef)]()

  def receive = {

    case Get ⇒ Try(workToDo.dequeue).toOption foreach {
      case (work, requester) ⇒ worker ! Do(work, requester)
    }

    case work ⇒ {
      workToDo += (work -> sender)
      worker ! WorkAvailable
    }
  }
}
