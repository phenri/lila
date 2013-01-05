package lila
package ladder

import user.User

import com.novus.salat.annotations.Key
import org.joda.time.DateTime

case class Lad(
    @Key("_id") id: String,
    user: String,
    ladder: String,
    pos: Int,
    hist: List[Int]) {

  def history: List[(DateTime, Int)] = (hist grouped 2).toList collect {
    case List(ts, p) ⇒ new DateTime(ts * 1000) -> p
  }

  def addPos(p: Int) = copy(
    pos = p,
    hist = DateTime.now.getSeconds.toInt :: p :: hist
  )
}

object Lad {

  def makeId(ladder: String, user: String) = user + "@" + ladder

  def apply(ladder: String, user: String, pos: Int): Lad = Lad(
    id = makeId(ladder, user),
    user = user,
    ladder = ladder,
    pos = pos,
    hist = Nil
  ) addPos pos
}

case class LadWithUser(lad: Lad, user: User) {
  def ladder = lad.ladder
  def pos = lad.pos
}
