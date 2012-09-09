package lila
package tournament

import org.joda.time.DateTime
import org.scala_tools.time.Imports._

import user.User

final class TournamentApi(
    repo: TournamentRepo) {

  def makeHook(setup: HookSetup, me: User): IO[Hook] = {
    for {
      lastOption ← repo.lastTournament
      lastIdle = (lastOption map (_.endsAt)) | DateTime.now
      startsAt = lastIdle + 5.minutes
      tournament = Tournament(
        createdBy = me.id,
        startsAt = startsAt,
        minutes = setup.minutes)
      _ ← repo saveIO tournament
    } yield tournament
  }
}
