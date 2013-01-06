package lila
package ladder

import ActorApi._
import game.DbGame
import socket._
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

    case Join(ladderId, userId) ⇒ ladRepo.lastPos(ladderId) flatMap { pos ⇒
      ladRepo.insertIO(Lad(ladderId, userId, pos + 1))
    } unsafePerformIO

    case Reload     ⇒ notifyReload

    case Close ⇒ {
      members.values foreach { _.channel.end() }
      self ! PoisonPill
    }
  }

  private def notifyReload {
    notifyVersion("reload", JsNull)
  }
}
