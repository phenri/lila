package lila.ai
package stockfish

import scala.concurrent.duration._

import akka.actor.ActorRef
import akka.pattern.ask
import chess.format.Forsyth

import actorApi._
import lila.analyse.Evaluation
import lila.analyse.Info
import lila.hub.actorApi.ai.GetLoad

private[ai] final class Server(
    queue: ActorRef,
    config: Config,
    host: Fu[AiHost],
    val uciMemo: lila.game.UciMemo) extends lila.ai.Ai {

  def move(uciMoves: List[String], initialFen: Option[String], level: Int): Fu[MoveResult] = {
    implicit val timeout = makeTimeout(config.playTimeout)
    queue ? PlayReq(uciMoves, initialFen map chess960Fen, level) mapTo
      manifest[Option[String]] flatten "[stockfish] play failed" flatMap { move ⇒
        host map { MoveResult(move, _) }
      }
  }

  def analyse(uciMoves: List[String], initialFen: Option[String]): Fu[List[Info]] = {
    implicit val timeout = makeTimeout(config.analyseTimeout)
    (queue ? FullAnalReq(uciMoves, initialFen map chess960Fen)) mapTo
      manifest[Option[List[Info]]] flatten "[stockfish] analyse failed"
  }

  def analysePosition(fen: String): Fu[String] = {
    implicit val timeout = makeTimeout(config.analyseTimeout)
    (Forsyth <<< fen) match {
      case Some(game) if game.situation.playable(true) ⇒
        (queue ? PositionAnalReq(fen)) mapTo
          manifest[Option[Evaluation]] flatten
          s"[stockfish] analyse position failed: $fen" map { eval ⇒
            game.situation.color.fold(eval, -eval).showWithoutLine
          }
      case _ ⇒ fufail(s"[stockfish] analyse position: unplayable")
    }
  }

  def load: Fu[Int] = {
    import makeTimeout.short
    queue ? GetLoad mapTo manifest[Int] addEffect { l ⇒
      (l > 0) ! println(s"[stockfish] load = $l")
    }
  }

  private def chess960Fen(fen: String) = (Forsyth << fen).fold(fen) { situation ⇒
    fen.replace("KQkq", situation.board.pieces.toList filter {
      case (_, piece) ⇒ piece is chess.Rook
    } sortBy {
      case (pos, _) ⇒ (pos.y, pos.x)
    } map {
      case (pos, piece) ⇒ piece.color.fold(pos.file.toUpperCase, pos.file)
    } mkString "")
  }
}
