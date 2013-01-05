package lila
package ui

import i18n.I18nKeys
import controllers.routes
import user.User

import play.api.mvc.Call

final class SiteMenu(trans: I18nKeys) {

  import SiteMenu._

  val play = Elem("play", routes.Lobby.home, trans.play)
  val game = Elem("game", routes.Game.realtime, trans.games)
  val tournament = Elem("tournament", routes.Tournament.home, trans.tournament)
  val ladder = Elem("ladder", routes.Ladder.home, trans.ladder)
  val user = Elem("user", routes.User.list(page = 1), trans.people)
  val team = Elem("team", routes.Team.home(page = 1), trans.teams)
  val forum = Elem("forum", routes.ForumCateg.index, trans.forum)
  val message = Elem("message", routes.Message.inbox(page = 1), trans.inbox)

  private val authenticated = List(play, game, tournament, ladder, user, team, forum, message)
  private val anonymous = List(play, game, tournament, ladder, user, team, forum)

  def all(me: Option[User]) = me.isDefined.fold(authenticated, anonymous)
}

object SiteMenu {

  sealed case class Elem(code: String, route: Call, name: I18nKeys#Key) {

    def currentClass(e: Option[Elem]) = if (e == Some(this)) " current" else ""
  }
}
