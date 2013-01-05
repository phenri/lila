package lila
package ladder

case class LadderViewLite(
  ladder: Ladder,
  leader: Option[Lad],
  nbLads: Int)
