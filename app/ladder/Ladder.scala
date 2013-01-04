package lila
package ladder

import com.novus.salat.annotations.Key

case class Ladder(
  @Key("_id") id: String, // also the url slug
  name: String) {

}
