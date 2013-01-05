package lila
package ladder

case class LadderViewLite(
  ladder: Ladder,
  leader: Option[Lad])

case class LadderView(
  ladder: Ladder,
  leader: Option[Lad])
