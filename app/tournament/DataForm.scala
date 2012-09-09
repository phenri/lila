package lila
package tournament

import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._

final class DataForm {

  import lila.core.Form._

  val hook = Form(mapping(
    "minutes" -> numberIn(Tournament.minuteChoices),
    "minPlayers" -> numberIn(Tournament.minPlayerChoices)
  )(HookSetup.apply)(HookSetup.unapply)) fill HookSetup()
}

case class HookSetup(
  minutes: Int = Hook.minuteDefault,
  minPlayers: Int = Hook.minPlayerDefault)
