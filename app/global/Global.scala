package global

import java.util
import java.util.concurrent.Callable
import scala.concurrent._
import play.api._
import play.inject.ApplicationLifecycle
import play.libs.F.Promise
import neo4jplugin.Neo4jPlugin

object Global extends GlobalSettings {

//  @volatile private var hooks = List.empty[() => Future[Unit]]
  val hooks = List[Callable[Promise[Void]]]()

  override def onStart(app: Application): Unit = {
    super.onStart(app)
    new Neo4jPlugin(new ApplicationLifecycle {
      override def addStopHook(hook: Callable[Promise[Void]]): Unit = hooks :+ hook
    })
  }

  override def onStop(app: Application): Unit = {
    import play.api.libs.iteratee.Execution.Implicits.trampoline
    super.onStop(app)
    hooks.map(f => f.call())
  }
}