package lila
package tournament

import com.novus.salat._
import com.novus.salat.dao._
import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.query.Imports._
import scalaz.effects._
import org.joda.time.DateTime
import org.scala_tools.time.Imports._

class HookRepo(collection: MongoCollection)
    extends SalatDAO[Hook, String](collection) {

  def byId(id: String): IO[Option[Hook]] = io {
    findOneById(id)
  }

  def saveIO(hook: Hook): IO[Unit] = io {
    save(hook)
  }

  val lastHook: IO[Option[Hook]] = io {
    find(DBObject()).sort(sortCreated).limit(1).toList.headOption
  }

  private def sortCreated = DBObject("createdAt" -> -1)
}
