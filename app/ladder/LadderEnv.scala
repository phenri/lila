package lila
package ladder

import user.UserRepo
import core.Settings

import com.mongodb.casbah.MongoCollection
import scalaz.effects._

final class LadderEnv(
    userRepo: UserRepo,
    settings: Settings,
    mongodb: String ⇒ MongoCollection) {

  import settings._

  private lazy val ladderRepo = new LadderRepo(mongodb(LadderCollectionLadder))
  private lazy val ladRepo = new LadRepo(mongodb(LadderCollectionLad))

  private lazy val paginator = new PaginatorBuilder(
    ladRepo = ladRepo,
    userRepo = userRepo,
    maxPerPage = LadderPaginatorMaxPerPage)

  lazy val api = new LadderApi(
    ladderRepo = ladderRepo,
    ladRepo = ladRepo,
    paginator = paginator)
}
