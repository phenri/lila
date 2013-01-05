package lila
package ladder

import core.Settings

import com.mongodb.casbah.MongoCollection
import scalaz.effects._

final class LadderEnv(
    settings: Settings,
    mongodb: String ⇒ MongoCollection) {

  import settings._

  private lazy val ladderRepo = new LadderRepo(mongodb(LadderCollectionLadder))
  private lazy val ladRepo = new LadRepo(mongodb(LadderCollectionLad))

  lazy val api = new LadderApi(
    ladderRepo = ladderRepo,
    ladRepo = ladRepo)
}
