package lila.hub
package pull

import akka.actor.ActorRef

object api {

  type WorkerReceive = PartialFunction[(Any, ActorRef), Unit]

  case class Register(x: ActorRef)
  case object WorkAvailable
  case object Get
  case class Do(x: Any, requester: ActorRef)
  case object Done
}
