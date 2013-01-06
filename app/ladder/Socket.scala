package lila
package ladder

import akka.actor._
import akka.pattern.ask
import akka.util.duration._
import akka.util.Timeout
import akka.dispatch.Await
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import play.api.Play.current
import scalaz.effects._

import ActorApi._
import user.User
import game.DbGame
import socket.{ GetHub, GetHubVersion, PingVersion, Quit, LiveGames }
import socket.Util.connectionFail
import implicits.RichJs._

private[ladder] final class Socket(
    getLadder: String ⇒ IO[Option[Ladder]],
    hubMaster: ActorRef) {

  private val timeoutDuration = 1 second
  implicit private val timeout = Timeout(timeoutDuration)

  def join(
    ladderId: String,
    version: Option[Int],
    uid: Option[String],
    user: Option[User]): IO[SocketPromise] =
    getLadder(ladderId) map { ladderOption ⇒
      ((ladderOption |@| uid |@| version) apply {
        (ladder: Ladder, uid: String, version: Int) ⇒
          (for {
            hub ← hubMaster ? GetHub(ladderId) mapTo manifest[ActorRef]
            socket ← hub ? Join(uid, user map (_.id)) map {
              case Connected(enumerator, member) ⇒ (
                Iteratee.foreach[JsValue](
                  controller(hub, uid, member, ladderId)
                ) mapDone { _ ⇒
                    hub ! Quit(uid)
                  },
                  enumerator)
            }
          } yield socket).asPromise: SocketPromise
      }) | connectionFail
    }

  private def controller(
    hub: ActorRef,
    uid: String,
    member: Member,
    ladderId: String): JsValue ⇒ Unit = e ⇒ e str "t" match {
    case Some("p") ⇒ e int "v" foreach { v ⇒
      hub ! PingVersion(uid, v)
    }
    case _ ⇒
  }

  def blockingVersion(id: String): Int = Await.result(
    hubMaster ? GetHubVersion(id) mapTo manifest[Int],
    timeoutDuration)
}
