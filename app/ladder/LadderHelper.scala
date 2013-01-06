package lila
package ladder

import http.Context
import user.User

import com.codahale.jerkson.Json
import scala.math.{ min, max, round }

trait LadderHelper {

  def ladderJsData(
    l: Ladder,
    version: Int,
    user: Option[User]) = Json generate {

    Map(
      "ladder" -> Map(
        "id" -> l.id
      ),
      "version" -> version
    ).combine(user) { (map, u) ⇒
        map + ("username" -> u.username)
      }
  }
}
