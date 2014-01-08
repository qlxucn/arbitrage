import akka.actor.Props
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import play.api._
import play.api.libs.concurrent.Akka
import play.api.mvc._
import play.api.mvc.Results._
import play.api.Play.current
import actor.MarginMonitActor
import actor.MarginMonitMsg


object Global extends GlobalSettings {

  override def onStart(app: Application) {
    val actorRef = Akka.system.actorOf(Props[MarginMonitActor])
    import play.api.libs.concurrent.Execution.Implicits._
    Akka.system.scheduler.schedule(
      FiniteDuration(0, TimeUnit.SECONDS),
      FiniteDuration(5, TimeUnit.MINUTES),
//      FiniteDuration(5, TimeUnit.SECONDS),
      actorRef, MarginMonitMsg("hi"))

    Logger.info("Application has started")
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

  override def onError(request: RequestHeader, ex: Throwable) = {
    InternalServerError(
      views.html.error_page(ex.getMessage)
    )
  }

  override def onHandlerNotFound(request: RequestHeader): Result = {
    NotFound(
      views.html.error_page("Not found page: " + request.path)
    )
  }

  override def onBadRequest(request: RequestHeader, error: String) = {
    BadRequest("Bad Request: " + error)
  }
}
