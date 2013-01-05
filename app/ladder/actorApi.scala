package lila
package ladder

import socket.SocketMember

private[ladder] object ActorApi {

  case class Member(
    channel: JsChannel,
    username: Option[String]) extends SocketMember

  case class Join(ladderId: String, userId: String)

  case class Reorder(lad1: Lad, lad2: Lad)

  case object Reload
}
