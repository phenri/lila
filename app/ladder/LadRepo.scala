package lila
package ladder

import com.novus.salat._
import com.novus.salat.dao._
import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.Imports._
import scalaz.effects._

// db.lad.ensureIndex({user:1)
// db.lad.ensureIndex({ladder:1, pos:-1}, {unique: true})
private[ladder] final class LadRepo(collection: MongoCollection)
    extends SalatDAO[Lad, String](collection) {

  def byLadderId(ladderId: String): IO[List[Lad]] = io {
    find(ladderIdQuery(ladderId)).sort(sortQuery()).toList
  }

  def ladderLeader(ladderId: String): IO[Option[Lad]] = io {
    find(ladderIdQuery(ladderId)).sort(sortQuery()).limit(1).toList.headOption
  }

  def countByLadderId(ladderId: String): IO[Int] = io {
    count(ladderIdQuery(ladderId)).toInt
  }

  def exists(ladderId: String, userId: String): IO[Boolean] = io {
    collection.find(idQuery(ladderId, userId)).limit(1).size != 0
  }

  def lastPos(ladderId: String): IO[Int] = io {
    ~(collection.find(ladderIdQuery(ladderId), DBObject("pos" -> true))
      .sort(sortQuery(-1))
      .limit(1) map { obj ⇒
        obj.getAs[Int]("pos")
      }).flatten.toList.headOption
  } 

  def insertIO(lad: Lad) = io { insert(lad, WriteConcern.Safe) }

  def ladderIdQuery(ladderId: String) = DBObject("ladder" -> ladderId)
  def userIdQuery(userId: String) = DBObject("user" -> userId)
  def idQuery(ladderId: String, userId: String) = DBObject("_id" -> id(ladderId, userId))
  def id(ladderId: String, userId: String) = Lad.makeId(ladderId, userId)
  def sortQuery(order: Int = 1) = DBObject("pos" -> order)
}
