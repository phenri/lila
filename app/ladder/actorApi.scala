package lila
package ladder

import user.User
import socket.SocketMember

private[ladder] object ActorApi {

  case class Member(channel: JsChannel, username: Option[String]) extends SocketMember

  case class JoinLadder(ladderId: String, userId: String)

  case class Reorder(lad1: Lad, lad2: Lad)

  case object Reload

  case class Join(uid: String, userId: Option[String])
  case class Connected(enumerator: JsEnumerator, member: Member)
}
