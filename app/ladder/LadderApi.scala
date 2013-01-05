package lila
package ladder

import scalaz.effects._

final class LadderApi(
    ladderRepo: LadderRepo,
    ladRepo: LadRepo) {

  val ladders: IO[List[LadderViewLite]] = ladderRepo.findAll.flatMap(_.map(ladder ⇒
    for {
      leader ← ladRepo ladderLeader ladder.id
      nbLads ← ladRepo countByLadderId ladder.id
    } yield LadderViewLite(ladder, leader, nbLads)
  ).sequence)
}
