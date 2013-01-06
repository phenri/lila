package lila
package ladder

import ActorApi._
import socket.{ GetHub, GetHubVersion, History }

import akka.actor._
import akka.actor.ReceiveTimeout
import akka.util.duration._
import akka.util.Timeout
import akka.pattern.{ ask, pipe }
import akka.dispatch.{ Future, Promise }
import play.api.libs.json._
import play.api.libs.concurrent._
import play.api.Play.current

private[ladder] final class HubMaster(
    ladRepo: LadRepo,
    makeHistory: () ⇒ History,
    uidTimeout: Int,
    hubTimeout: Int) extends Actor {

  private implicit val timeout = Timeout(1 second)
  private implicit val executor = Akka.system.dispatcher

  private var hubs = Map.empty[String, ActorRef]

  def receive = {

    case msg @ JoinLadder(id, _) ⇒ getHub(id) ! msg

    case GetHub(id: String)      ⇒ sender ! getHub(id)

    case msg @ GetHubVersion(id) ⇒ (hubs get id).fold(_ ? msg pipeTo sender, sender ! 0)
  }

  private def getHub(id: String) = (hubs get id) | {
    mkHub(id) ~ { h ⇒ hubs = hubs + (id -> h) }
  }

  private def mkHub(id: String): ActorRef =
    context.actorOf(Props(new Hub(
      ladRepo = ladRepo,
      ladderId = id,
      history = makeHistory(),
      uidTimeout = uidTimeout,
      hubTimeout = hubTimeout
    )), name = "ladder_hub_" + id)
}
