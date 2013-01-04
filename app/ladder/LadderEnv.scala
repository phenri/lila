package lila
package ladder

import core.Settings

import com.mongodb.casbah.MongoCollection
import scalaz.effects._

final class LadderEnv(
    settings: Settings,
    mongodb: String ⇒ MongoCollection) {

  import settings._

  lazy val ladderRepo = new LadderRepo(mongodb(LadderCollectionLadder))
}
