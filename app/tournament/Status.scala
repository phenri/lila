package lila
package tournament

object Status extends Enumeration {
  type Status = Value
  val Created, Started, Finished = Value
}
