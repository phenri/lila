package lila
package ai.stockfish

import model._
import model.analyse._

import akka.actor.{ Props, Actor, ActorRef, FSM ⇒ AkkaFSM, LoggingFSM }
import akka.util.duration._
import scalaz.effects._

final class ActorFSM(
  processBuilder: Process.Builder,
  config: Config)
    extends Actor with LoggingFSM[State, Data] {

  var process: Process = _

  override def preStart() {
    process = processBuilder(
      out ⇒ self ! Out(out),
      err ⇒ self ! Err(err),
      config.debug)
  }

  startWith(Starting, Todo())

  when(Starting) {
    case Event(Out(t), _) if t startsWith "Stockfish" ⇒ {
      process write "uci"
      stay
    }
    case Event(Out("uciok"), data) ⇒ {
      config.init foreach process.write
      nextTask(data)
    }
    case Event(task: analyse.Task.Builder, data) ⇒
      stay using (data enqueue task(sender))
    case Event(task: play.Task.Builder, data) ⇒
      stay using (data enqueue task(sender))
  }
  when(Idle) {
    case Event(Out(t), data) ⇒ {
      log.warning("[%s] %s".format(data.size, t));
      stay
    }
  }
  when(IsReady) {
    case Event(Out("readyok"), doing: Doing) ⇒ {
      val lines = config go doing.current
      if (nowSeconds % 20 != 0) {
        lines foreach process.write
      }
      goto(Running)
    }
  }
  when(Running, stateTimeout = 1 second) {
    case Event(Out(t), doing: Doing) if t startsWith "info depth" ⇒
      stay using (doing map (_.right map (_ buffer t)))
    case Event(Out(t), doing: Doing) if t startsWith "bestmove" ⇒
      doing.current.fold(
        play ⇒ {
          play.ref ! model.play.BestMove(t.split(' ') lift 1)
          nextTask(doing.done)
        },
        anal ⇒ (anal buffer t).flush.fold(
          err ⇒ {
            log error err.shows
            anal.ref ! failure(err)
            nextTask(doing.done)
          },
          task ⇒ task.isDone.fold({
            task.ref ! success(task.analysis.done)
            nextTask(doing.done)
          },
            nextTask(doing.done enqueue task)
          )
        )
      )
    case Event(StateTimeout, doing: Doing) => {
      log.warning("ActorFSM state timeout")
      nextTask(doing.done)
    }
  }
  whenUnhandled {
    case Event(task: analyse.Task.Builder, data) ⇒ {
      display(data)(task.toString)
      nextTask(data enqueue task(sender))
    }
    case Event(task: play.Task.Builder, data)    ⇒ {
      display(data)(task.toString)
      nextTask(data enqueue task(sender))
    }
    case Event(Out(t), _)                        ⇒ stay
    case Event(GetQueueSize, data)               ⇒ sender ! QueueSize(data.size); stay
  }

  def nextTask(data: Data) = data.fold(
    todo ⇒ todo.doing(
      doing ⇒ {
        config prepare doing.current foreach process.write
        goto(IsReady) using doing
      },
      todo ⇒ goto(Idle) using todo
    ),
    doing ⇒ stay using doing
  )

  private def display(data: Data)(msg: String) {
    println("[ActorFSM](%d) %s".format(data.size, msg))
  }

  override def postStop() {
    process.destroy()
    process = null
  }
}
