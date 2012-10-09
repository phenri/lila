package lila
package ai
package stockfish

import model._
import model.analyse._

import akka.actor.{ Props, Actor }
import akka.util.duration._
import scalaz.effects._

final class EngineActor(execPath: String, config: Config) extends Actor {

  private lazy val process = Process(execPath, "StockFish") _

  private lazy val fsm = context.actorOf(Props(
    new ActorFSM(process, config)
  ))

  def receive = {
    
    case playBuilder: play.Task.Builder => {
      sender ! 
    }
  }
}
