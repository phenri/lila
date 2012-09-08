package lila
package elo

import scalaz.effects._
import com.codahale.jerkson.Json
import org.joda.time.DateTime
import org.scala_tools.time.Imports._

final class AverageChart(elos: List[(DateTime, Int)]) {

  def columns = EloChart.columns

  def rows = Json generate {
    withMedian(reduce(elos)) map {
      case (ts, elo, med) ⇒ List(date(ts), elo, med)
    }
  }

}

object AverageChart {

  val columns = Json generate List(
    "string" :: "Date" :: Nil,
    "number" :: "Average Elo" :: Nil)

  def apply(rangeElo: (DateTime, DateTime) => Int): IO[Option[EloChart]] =
    gameRe
    historyRepo userElos user.username map { elos ⇒
      (elos.size > 1) option { new EloChart(elos) }
    }
}
