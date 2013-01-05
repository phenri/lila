package lila
package ladder

import com.novus.salat.annotations.Key
import org.joda.time.DateTime
import ornicar.scalalib.Random

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

  def apply(user: String, ladder: String, pos: Int): Lad = Lad(
    id = Random nextString 8,
    user = user,
    ladder = ladder,
    pos = pos,
    hist = Nil
  ) addPos pos
}
