package lila
package ladder

import user.UserRepo
import core.Settings
import socket.History

import com.mongodb.casbah.MongoCollection
import scalaz.effects._
import akka.actor.Props
import play.api.libs.concurrent._
import play.api.Application

final class LadderEnv(
    app: Application,
    userRepo: UserRepo,
    onlineUsernames: () => Iterable[String],
    settings: Settings,
    mongodb: String ⇒ MongoCollection) {

  import settings._

  private implicit val ctx = app

  lazy val api = new LadderApi(
    ladderRepo = ladderRepo,
    ladRepo = ladRepo,
    paginator = paginator,
    socket = socket,
    hubMaster = hubMaster,
    onlineUsernames = onlineUsernames,
    challengeRadius = 20)

  private lazy val ladderRepo = new LadderRepo(mongodb(LadderCollectionLadder))
  private lazy val ladRepo = new LadRepo(mongodb(LadderCollectionLad))

  private lazy val paginator = new PaginatorBuilder(
    ladRepo = ladRepo,
    userRepo = userRepo,
    maxPerPage = LadderPaginatorMaxPerPage)

  private lazy val history = () ⇒ new History(timeout = LadderMessageLifetime)

  private lazy val socket = new Socket(
    getLadder = ladderRepo.byId,
    hubMaster = hubMaster)

  private lazy val hubMaster = Akka.system.actorOf(Props(new HubMaster(
    ladRepo = ladRepo,
    makeHistory = history,
    uidTimeout = LadderUidTimeout,
    hubTimeout = LadderHubTimeout
  )), name = ActorLadderHubMaster)
}
