package lila
package setup

import chess.Mode

trait HumanConfig extends Config {

  // casual or rated
  val mode: Mode
}

trait BaseHumanConfig extends BaseConfig {

  val LADDER_MODE_ID = 9

  val modes = Mode.all map (_.id)
  val modesWithLadder = modes :+ LADDER_MODE_ID
}
