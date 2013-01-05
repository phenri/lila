package lila
package ladder

import com.novus.salat._
import com.novus.salat.dao._
import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.query.Imports._
import scalaz.effects._

private[ladder] final class LadRepo(collection: MongoCollection)
    extends SalatDAO[Lad, String](collection) {

  def byId(id: String): IO[Option[Lad]] = io { findOneById(id) }

  def byLadderId(ladderId: String): IO[List[Lad]] = io {
    find(ladderIdQuery(ladderId)).sort(sortQuery()).toList
  }

  def ladderLeader(ladderId: String): IO[Option[Lad]] = io {
    find(ladderIdQuery(ladderId)).sort(sortQuery()).limit(1).toList.headOption
  }

  def countByLadderId(ladderId: String): IO[Int] = io {
    count(ladderIdQuery(ladderId)).toInt
  }

  private def ladderIdQuery(ladderId: String) = DBObject("ladder" -> ladderId)
  private def sortQuery(order: Int = -1) = DBObject("pos" -> order)
}
