package controllers

import lila._
import views._
import http.Context

import scalaz.effects._
import play.api.mvc.Result

object Tournament extends LilaController {

  val repo = env.tournament.repo
  val forms = env.tournament.forms
  val api = env.tournament.api

  val home = Open { implicit ctx ⇒
    IOk(repo.created map { tournaments ⇒
      html.tournament.home(tournaments)
    })
  }

  def show(id: String) = Open { implicit ctx ⇒
    IOptionOk(repo byId id) { tournament ⇒
      tournament.status match {
        case Status.Created  ⇒ html.tournament.show.created(tournament)
        case Status.Started  ⇒ html.tournament.show.started(tournament)
        case Status.Finished ⇒ html.tournament.show.finished(tournament)
      }
    }
  }

  def form = Auth { implicit ctx ⇒
    me ⇒
      Ok(html.tournament.form(forms.create))
  }

  def create = AuthBody { implicit ctx ⇒
    implicit me ⇒
      IOResult {
        implicit val req = ctx.body
        forms.hook.bindFromRequest.fold(
          err ⇒ io(BadRequest(html.message.form(err))),
          setup ⇒ api.makeHook(setup, me).map(hook ⇒
            Redirect(routes.Tournament.home)
          ))
      }
  }
}
