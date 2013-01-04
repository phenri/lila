package lila
package ladder

case class Lad(
  @Key("_id") id: String, 
  user: String,
  ladder: String,
  pos: Int,
  hist: List[Int]) {

}
