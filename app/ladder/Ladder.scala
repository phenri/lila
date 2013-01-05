package lila
package ladder

import com.novus.salat.annotations.Key
import org.joda.time.DateTime

case class Ladder(
  @Key("_id") id: String, // also the url slug
  name: String,
  desc: String,
  createdAt: DateTime) {

}
