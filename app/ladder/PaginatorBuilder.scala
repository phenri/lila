package lila
package ladder

import user.{ User, UserRepo }
import mongodb.CachedAdapter

import com.github.ornicar.paginator._
import com.mongodb.casbah.Imports._
import org.joda.time.DateTime

final class PaginatorBuilder(
    ladRepo: LadRepo,
    userRepo: UserRepo,
    maxPerPage: Int) {

  def ladderLads(ladder: Ladder, page: Int): Paginator[LadWithUser] =
    paginator(new LadAdapter(
      ladder,
      ladRepo ladderIdQuery ladder.id
    ), page, maxPerPage)

  def challengers(
    ladder: Ladder,
    pos: Int,
    radius: Int,
    userIds: Iterable[String],
    page: Int): Paginator[LadWithUser] =
    paginator(new LadAdapter(
      ladder,
      ladRepo.challengersQuery(ladder.id, pos, radius, userIds)
    ), page, maxPerPage)

  private def paginator[A](adapter: Adapter[A], page: Int, mpp: Int): Paginator[A] =
    Paginator(
      adapter,
      currentPage = page,
      maxPerPage = mpp
    ) | paginator(adapter, 1, mpp)

  private final class LadAdapter(ladder: Ladder, query: DBObject) extends Adapter[LadWithUser] {

    val nbResults = ladder.nbLads

    def slice(offset: Int, length: Int): Seq[LadWithUser] = {
      val lads = (ladRepo find query sort sort skip offset limit length).toList
      val users = (userRepo byOrderedIds lads.map(_.user)).unsafePerformIO
      lads zip users map {
        case (lad, user) ⇒ LadWithUser(lad, user)
      }
    }

    private def sort = ladRepo sortQuery 1
  }
}
