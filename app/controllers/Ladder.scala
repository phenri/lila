package controllers

import lila._
import views._
import ladder.{ Ladder ⇒ LadderModel }

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
      for {
        ladOption ← ~ctx.me.map(u ⇒ api.lad(view.ladder.id, u.id))
      } yield html.ladder.show(
        view,
        api.lads(view.ladder, page),
        ladOption.map(lad ⇒ api.challengers(view.ladder, lad, page)),
        version(view.ladder))
    }
  }

  def join(id: String) = Auth { implicit ctx ⇒
    me ⇒ IOResult(api.join(id, me) map {
      case Some(l) ⇒ Redirect(routes.Ladder.show(l.id))
      case _       ⇒ notFound
    })
  }

  def websocket(id: String) = WebSocket.async[JsValue] { req ⇒
    implicit val ctx = reqToCtx(req)
    api.websocket(id, getInt("version"), get("sri"), ctx.me).unsafePerformIO
  }

  private def version(ladder: LadderModel): Int = api ladderVersion ladder.id
}
