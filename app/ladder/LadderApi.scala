package lila
package ladder

import user.User

import scalaz.effects._

final class LadderApi(
    ladderRepo: LadderRepo,
    ladRepo: LadRepo,
    paginator: PaginatorBuilder) {

  val ladders: IO[List[LadderViewLite]] = ladderRepo.findAll.flatMap(_.map(ladder ⇒
    ladRepo ladderLeader ladder.id map { LadderViewLite(ladder, _) }
  ).sequence)

  def ladder(id: String): IO[Option[LadderView]] = for {
    ladder ← ladderRepo byId id
    leader ← ~ladder.map(l ⇒ ladRepo ladderLeader l.id)
  } yield ladder map { LadderView(_, leader) }

  def lads(ladder: Ladder, page: Int) = paginator.ladderLads(ladder, page)

  def belongsTo(ladderId: String, userId: String): IO[Boolean] = ladRepo.exists(ladderId, userId)

  def ladderLastPos(ladderId: String): IO[Int] = ladRepo.lastPos(ladderId)

  def join(ladderId: String, user: User): IO[Option[Ladder]] =
    ladderRepo byId ladderId flatMap {
      _.fold(
        ladder ⇒ belongsTo(ladder.id, user.id) flatMap { joined ⇒
          ladderLastPos(ladder.id) flatMap { pos ⇒
            ladRepo.insertIO(Lad(ladder.id, user.id, pos)) >> ladderRepo.incLads(ladder.id, 1)
          } doUnless joined
        } inject ladder.some,
        io(None)
      )
    }
}
