package lila
package cli

import user.{ User, UserRepo }
import ladder.{ Ladder, LadderApi }
import scalaz.effects._

private[cli] case class Ladders(
    api: LadderApi,
    userRepo: UserRepo) {

  def massJoin(ladderId: String): IO[String] = perform(ladderId) { ladder ⇒
    for {
      users ← userRepo findEnableds 100
      _ ← users.map(api.join(ladderId, _)).sequence
    } yield ()
  }

  private def perform(ladderId: String)(op: Ladder ⇒ IO[Unit]) = for {
    ladderOption ← api ladder ladderId
    res ← ladderOption.map(_.ladder).fold(
      u ⇒ op(u) inject "Success",
      io("ladder not found")
    )
  } yield res
}
