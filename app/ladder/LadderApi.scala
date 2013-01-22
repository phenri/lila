package lila
package ladder

import ActorApi._
import game.DbGame
import user.User
import socket.GetHubVersion
import core.Futuristic.futureToIo
import templating.StringHelper

import scalaz.effects._
import akka.actor.ActorRef
import org.joda.time.DateTime

final class LadderApi(
    ladderRepo: LadderRepo,
    ladRepo: LadRepo,
    paginator: PaginatorBuilder,
    socket: Socket,
    hubMaster: ActorRef,
    challengeRadius: Int,
    onlineUsernames: () ⇒ Iterable[String]) {

  val ladders: IO[List[LadderViewLite]] = ladderRepo.findAll.flatMap(_.map(ladder ⇒
    ladRepo ladderLeader ladder.id map { LadderViewLite(ladder, _) }
  ).sequence)

  def ladder(id: String): IO[Option[LadderView]] = for {
    ladder ← ladderRepo byId id
    leader ← ~ladder.map(l ⇒ ladRepo ladderLeader l.id)
  } yield ladder map { LadderView(_, leader) }

  def lads(ladder: Ladder, page: Int) = paginator.ladderLads(ladder, page)

  def belongsTo(ladderId: String, userId: String): IO[Boolean] = ladRepo.exists(ladderId, userId)

  def lad(ladderId: String, userId: String): IO[Option[Lad]] = ladRepo.findOne(ladderId, userId)

  def challengers(ladder: Ladder, lad: Lad, page: Int) = paginator.challengers(
    ladder,
    lad.pos,
    (ladder.nbLads * challengeRadius) / 100,
    onlineUsernames(),
    page)

  def join(ladderId: String, user: User): IO[Option[Ladder]] = ladderRepo byId ladderId flatMap {
    ~_.map(ladder ⇒ belongsTo(ladder.id, user.id) flatMap { joined ⇒
      io(hubMaster ! JoinLadder(ladder.id, user.id)) >> ladderRepo.incLads(ladder.id, 1) doUnless joined
    } inject ladder.some)
  }

  def finishGame(game: DbGame): IO[Option[Ladder]] = for {
    ladderOption ← ~game.ladderId.map(ladderRepo.byId)
    result ← ~ladderOption.filter(_ ⇒ game.finished).map(ladder ⇒ {
      putStrLn(ladder.toString) inject ladder.some
    })
  } yield result

  def ladderVersion(id: String): Int = socket blockingVersion id

  def websocket(id: String, version: Option[Int], uid: Option[String], user: Option[User]) =
    socket.join(id, version, uid, user)

  def create(name: String, desc: String): IO[Unit] = Ladder(
    id = StringHelper slugify name,
    name = name,
    desc = desc,
    nbLads = 0,
    nbGames = 0,
    createdAt = DateTime.now) |> ladderRepo.insertIO

}
