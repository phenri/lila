package lila
package mongodb

import scala.annotation.tailrec

object PgnBinary {

  def encode(pgn: String): Array[Byte] = {
    // val ints = pgnToInts(pgn).pp
    // val bools = ints flatMap intToBools
    // val byteBools = (bools grouped 8).toList
    // println(byteBools map showBools)
    val hugeInt = pgnToInt(pgn).pp
    // val bytes = ints grouped 4 map { 
    //   case Nil => 0
    //   case a :: Nil => a
    //   case a :: b :: Nil => a + 32 
    Array.empty
  }

  def decode(bytes: Array[Byte]): String = ""

  private def pgnToInts(str: String): List[Int] = {
    val buffer = scala.collection.mutable.ListBuffer[Int]()
    var pgn = str
    while (pgn.nonEmpty) {
      stringToInt get (pgn take 1) map (i ⇒ (1 -> i)) orElse {
        if (pgn startsWith "O-O-O") stringToInt get "O-O-O" map (i ⇒ 5 -> i)
        else if (pgn startsWith "O-O") stringToInt get "O-O" map (i ⇒ 3 -> i)
        else None
      } match {
        case Some((drop, int)) => {
          pgn = pgn drop drop
          buffer += int
        }
        case _ => {
          println("Found invalid pgn: " + pgn take 5)
          pgn = ""
        }
      }
    }
    buffer.toList
  }

  // assumes i < 32
  private def intToBools(i: Int): List[Boolean] = {
    List(16, 8, 4, 2, 1) map (m ⇒ (i & m) > 0) 
  } ~ { bs => println(showBools(bs)) }

  private def showBools(bs: List[Boolean]) = bs map (_.fold("#", ".")) mkString ""

  private val symbols: List[String] = " " :: {
    ('a' to 'h').map(_.toString) ++
      (1 to 8).map(_.toString) ++
      "RNBQKPx=+#".toList.map(_.toString) ++
      Seq("O-O", "O-O-O")
  }.toList

  private val stringToInt: Map[String, Int] = symbols.zipWithIndex.toMap
  private val intToString: Map[Int, String] = (stringToInt map {
    case (a, b) ⇒ (b, a)
  }).toMap

  // utility methods for printing a byte array
  def showArray(b: Array[Byte]) = b.map(showByte).mkString(" ")
  def showByte(b: Byte) = pad(((b + 256) % 256).toHexString.toUpperCase)
  private def pad(s: String) = if (s.length == 1) "0" + s else s
}
