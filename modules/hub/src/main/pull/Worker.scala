package lila.hub
package pull

import akka.actor._
import api._

trait Worker extends Actor {

  private var master = none[ActorRef]

  def doWork: WorkerReceive

  def receive = idle

  def idle: Receive = {

    case Register(m)   ⇒ master = m.some

    case WorkAvailable ⇒ requestWork

    case Do(work, requester) ⇒ {
      context become busy
      _doWork(work, requester)
    }
  }

  def busy: Receive = {

    case Done ⇒ {
      context become idle
      requestWork
    }
  }

  private def fallback: WorkerReceive = {
    case (w, r) ⇒ {
      val error = s"pull.Worker unsupported work: $w"
      r ! Status.Failure(new Exception(error))
      self ! Done
    }
  }

  private val _doWork = doWork orElse fallback

  private def requestWork {
    master foreach { _ ! Get }
  }
}
