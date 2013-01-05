package lila
package ladder

import akka.actor._
import akka.util.duration._
import akka.util.Timeout
import akka.pattern.{ ask, pipe }
import akka.dispatch.{ Future, Promise }
import play.api.libs.concurrent._
import play.api.Play.current

private[ladder] final class Organizer(ladRepo: LadRepo) extends Actor {

  implicit val timeout = Timeout(1 second)

  def receive = {

    case Join(ladderId, userId) ⇒ ladRepo.lastPos(ladderId) flatMap { pos ⇒
      ladRepo.insertIO(Lad(ladderId, userId, pos + 1))
    } unsafePerformIO

    case Reorder(lad1, lad2)    ⇒
  }
}
