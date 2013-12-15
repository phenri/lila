package controllers

import scala.concurrent.Future

import lila.app._
import views._

object Roadmap extends LilaController {

  def index = Open { implicit ctx ⇒
    Future successful Ok(views.html.roadmap.index())
  }
}
