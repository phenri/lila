package lila
package ladder

import scalaz.effects._

final class LadderApi(
    ladderRepo: LadderRepo,
    ladRepo: LadRepo) {

  val ladders: IO[List[LadderViewLite]] = ladderRepo.findAll.flatMap(_.map(ladder ⇒
    ladRepo ladderLeader ladder.id map { LadderViewLite(ladder, _) }
  ).sequence)

  def ladder(id: String): IO[Option[LadderView]] = for {
    ladder <- ladderRepo byId id
  } yield LadderView(ladder)
}
