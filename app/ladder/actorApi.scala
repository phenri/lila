package lila
package ladder

private[ladder] case class Join(ladderId: String, userId: String)

private[ladder] case class Reorder(lad1: Lad, lad2: Lad)
