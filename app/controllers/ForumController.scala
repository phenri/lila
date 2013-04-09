package controllers

import lila.app._
import lila.user.Context
import lila.forum
import lila.team.Env.{ current ⇒ teamEnv }

import play.api.mvc._
import play.api.mvc.Results._

trait ForumController extends forum.Granter { self: LilaController ⇒

  protected def userBelongsToTeam(teamId: String, userId: String): Fu[Boolean] =
    teamEnv.api.belongsTo(teamId, userId)

  protected def CategGrantRead[A <: Result](categSlug: String)(a: ⇒ A)(implicit ctx: Context): Result =
    isGrantedRead(categSlug).fold(a,
      Forbidden("You cannot access to this category")
    )

  protected def CategGrantWrite[A <: Result](categSlug: String)(a: ⇒ A)(implicit ctx: Context): Result =
    isGrantedWrite(categSlug).await.fold(a,
      Forbidden("You cannot post to this category")
    )
}
