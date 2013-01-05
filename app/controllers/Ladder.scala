package controllers

import lila._
import views._

import scalaz.effects._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.concurrent._
import play.api.templates.Html

object Tournament extends LilaController {

  val home = Open { implicit ctx ⇒
    Async {
      futureTournaments zip userRepo.sortedByToints(10).toFuture map {
        case (((created, started), finished), leaderboard) ⇒
          Ok(html.tournament.home(created, started, finished, leaderboard))
      } asPromise
    }
  }
}
