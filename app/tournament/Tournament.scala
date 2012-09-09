package lila
package tournament

import org.joda.time.{ DateTime, Duration }
import org.scala_tools.time.Imports._
import com.novus.salat.annotations.Key
import ornicar.scalalib.OrnicarRandom

case class Tournament(
    @Key("_id") id: String,
    createdBy: String,
    startsAt: DateTime,
    minutes: Int,
    finished: Boolean = false,
    createdAt: DateTime = DateTime.now,
    players: List[String] = Nil) {

  lazy val duration = new Duration(minutes * 60 * 1000)

  lazy val endsAt = DateTime.now + duration

  def started = startsAt >= DateTime.now
  def ended = endsAt < DateTime.now

  def status = finished.fold(Status.Finished, started.fold(Status.Started, Status.Created))

  def showClock = "1 + 0"
}

object Tournament {

  def apply(hook: Hook): Tournament = new Tournament(
    id = hook.id,
    createdBy = hook.createdBy,
    startsAt = DateTime.now,
    minutes = hook.minutes,
    players = hook.players)
}
