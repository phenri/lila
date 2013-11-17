package lila.analyse

import akka.actor.ActorSelection
import com.typesafe.config.Config

import lila.common.PimpedConfig._
import lila.memo.AsyncCache

final class Env(
    config: Config,
    db: lila.db.Env,
    ai: ActorSelection,
    indexer: ActorSelection,
    nameUser: String ⇒ Fu[String]) {

  private val CollectionAnalysis = config getString "collection.analysis"
  private val NetDomain = config getString "net.domain"
  private val CachedNbTtl = config duration "cached.nb.ttl"
  private val CachedPositionMax = config getInt "cached.position.max"
  private val PaginatorMaxPerPage = config getInt "paginator.max_per_page"

  private[analyse] lazy val analysisColl = db(CollectionAnalysis)

  lazy val analyser = new Analyser(ai, indexer)
  lazy val positionAnalyser = new PositionAnalyser(ai, CachedPositionMax)

  lazy val paginator = new PaginatorBuilder(
    cached = cached,
    maxPerPage = PaginatorMaxPerPage)

  lazy val annotator = new Annotator(NetDomain)

  lazy val timeChart = TimeChart(nameUser) _

  lazy val cached = new {
    private val nbAnalysisCache = AsyncCache[Boolean, Int](
      _ => AnalysisRepo.count,
      timeToLive = CachedNbTtl)
    def nbAnalysis: Fu[Int] = nbAnalysisCache(true)
  }

  def cli = new lila.common.Cli {
    import tube.analysisTube
    def process = {
      case "analyse" :: "typecheck" :: Nil ⇒ lila.db.Typecheck.apply[Analysis](false)
    }
  }
}

object Env {

  lazy val current = "[boot] analyse" describes new Env(
    config = lila.common.PlayApp loadConfig "analyse",
    db = lila.db.Env.current,
    ai = lila.hub.Env.current.actor.ai,
    indexer = lila.hub.Env.current.actor.gameIndexer,
    nameUser = lila.user.Env.current.usernameOrAnonymous)
}
