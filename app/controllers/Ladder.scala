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

  def show(id: String) = Open { implicit ctx ⇒
    IOptionIOk(api ladder id) { ladder ⇒
      html.ladder show ladder
    }
  }
}
