package lila
package tournament

import org.joda.time.{ DateTime, Duration }
import org.scala_tools.time.Imports._
import com.novus.salat.annotations.Key
import ornicar.scalalib.OrnicarRandom

case class Hook(
    @Key("_id") id: String = OrnicarRandom nextString 8,
    createdBy: String,
    minutes: Int,
    minPlayers: Int,
    createdAt: DateTime = DateTime.now,
    openedAt: Option[DateTime] = None,
    closedAt: Option[DateTime] = None,
    players: List[String] = Nil) {

}

object Hook {
  
  import lila.core.Form._

  val minutes = 5 to 30 by 5
  val minuteDefault = 10
  val minuteChoices = options(minutes, "%d minute{s}")

  val minPlayers = 5 to 30 by 5
  val minPlayerDefault = 10
  val minPlayerChoices = options(minPlayers, "%d player{s}")
}
