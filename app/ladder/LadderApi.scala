package lila
package ladder

import game.DbGame
import user.User

import scalaz.effects._
import akka.actor.ActorRef

final class LadderApi(
    ladderRepo: LadderRepo,
    ladRepo: LadRepo,
    paginator: PaginatorBuilder,
    organizer: ActorRef) {

  import core.Futuristic.futureToIo

  val ladders: IO[List[LadderViewLite]] = ladderRepo.findAll.flatMap(_.map(ladder ⇒
    ladRepo ladderLeader ladder.id map { LadderViewLite(ladder, _) }
  ).sequence)

  def ladder(id: String): IO[Option[LadderView]] = for {
    ladder ← ladderRepo byId id
    leader ← ~ladder.map(l ⇒ ladRepo ladderLeader l.id)
  } yield ladder map { LadderView(_, leader) }

  def lads(ladder: Ladder, page: Int) = paginator.ladderLads(ladder, page)

  def belongsTo(ladderId: String, userId: String): IO[Boolean] = ladRepo.exists(ladderId, userId)

  def join(ladderId: String, user: User): IO[Option[Ladder]] = ladderRepo byId ladderId flatMap {
    ~_.map(ladder ⇒ belongsTo(ladder.id, user.id) flatMap { joined ⇒
      io(organizer ! Join(ladder.id, user.id)) >> ladderRepo.incLads(ladder.id, 1) doUnless joined
    } inject ladder.some)
  }

  def finishGame(game: DbGame): IO[Option[Ladder]] = for {
    ladderOption ← ~game.ladderId.map(ladderRepo.byId)
    result ← ~ladderOption.filter(_ ⇒ game.finished).map(ladder ⇒ {
      putStrLn(ladder.toString) inject ladder.some
    })
  } yield result
}
