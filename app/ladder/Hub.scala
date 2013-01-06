package lila
package ladder

import ActorApi._
import game.DbGame
import socket.{ HubActor, Historical, History, GetHubVersion }
import memo.BooleanExpiryMemo

import akka.actor._
import akka.util.duration._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.Play.current
import scalaz.effects._

final class Hub(
    ladderId: String,
    ladRepo: LadRepo,
    val history: History,
    uidTimeout: Int,
    hubTimeout: Int) extends HubActor[Member](uidTimeout) with Historical[Member] {

  private var lastPingTime = nowMillis

  def receiveSpecific = {

    case JoinLadder(ladderId, userId) ⇒ ladRepo.lastPos(ladderId) flatMap { pos ⇒
      ladRepo.insertIO(Lad(ladderId, userId, pos + 1))
    } unsafePerformIO

    case Join(uid, user) ⇒ {
      val (enumerator, channel) = Concurrent.broadcast[JsValue]
      val member = Member(channel, user)
      addMember(uid, member)
      sender ! Connected(enumerator, member)
    }

    case Reload     ⇒ notifyReload

    case GetHubVersion(_) ⇒ sender ! history.version
  }

  private def notifyReload {
    notifyVersion("reload", JsNull)
  }
}
