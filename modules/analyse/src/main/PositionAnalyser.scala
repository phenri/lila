package lila.analyse

import akka.actor.ActorSelection
import akka.pattern.ask
import chess.format.Forsyth

import lila.memo.AsyncCache

private[analyse] final class PositionAnalyser(ai: ActorSelection, cacheMax: Int) {

  private val cache = AsyncCache(compute, maxCapacity = cacheMax)

  def apply(fen: String): Fu[Option[String]] = cache(fen)

  private def compute(fen: String): Fu[Option[String]] = {
    implicit val timeout = makeTimeout.seconds(30)
    ai ? lila.hub.actorApi.ai.AnalysePosition(fen) mapTo
      manifest[String] map (_.some) recover { case _ ⇒ none }
  }
}
