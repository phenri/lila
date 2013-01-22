package lila
package cli

import user.{ User, UserRepo }
import ladder.{ Ladder, LadderApi }
import scalaz.effects._

private[cli] case class Ladders(api: LadderApi, userRepo: UserRepo) {

  def create(name: String): IO[String] = api.create(name, "") inject "Ladder created"

  def massJoin(ladderId: String): IO[String] = perform(ladderId) { ladder ⇒
    for {
      users ← userRepo findEnableds 100
      _ ← users.map(api.join(ladderId, _)).sequence
    } yield ()
  }

  def join(ladderId: String, userId: String): IO[String] = perform(ladderId) { ladder ⇒
    userRepo byId userId flatMap { user ⇒
      ~user.map(api.join(ladderId, _).void)
    }
  }

  private def perform(ladderId: String)(op: Ladder ⇒ IO[Unit]) = for {
    ladderOption ← api ladder ladderId
    res ← ladderOption.map(_.ladder).fold(
      u ⇒ op(u) inject "Success",
      io("ladder not found")
    )
  } yield res
}
