package controllers

import lila._
import views._

import scalaz.effects._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.concurrent._
import play.api.templates.Html

object Ladder extends LilaController {

  private def api = env.ladder.api

  val home = Open { implicit ctx ⇒
    IOk(api.ladders map { ladders ⇒
      html.ladder home ladders
    })
  }

  def show(id: String, page: Int) = Open { implicit ctx ⇒
    IOptionIOk(api ladder id) { view ⇒
      ~ctx.me.map(u ⇒ api.belongsTo(view.ladder.id, u.id)) map { joined ⇒
        html.ladder.show(view, api.lads(view.ladder, page), joined)
      }
    }
  }

  def join(id: String) = Auth { implicit ctx ⇒
    me ⇒ IOResult(api.join(id, me) map {
      case Some(l) ⇒ Redirect(routes.Ladder.show(l.id))
      case _       ⇒ notFound
    })
  }
}
