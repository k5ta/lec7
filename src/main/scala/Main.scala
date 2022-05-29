import shortener.api.{LinksShortenerService, LinksShortenerServiceLive}
import shortener.logic.LinksManagerLive
import shortener.storage.ShortenedLinksDaoLive
import zio._
import zhttp.service.Server

object Main extends App {

  private val applicationLayer =
    ShortenedLinksDaoLive.live >>> LinksManagerLive.live ++ zio.console.Console.live >+> LinksShortenerServiceLive.live

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    LinksShortenerService(_.asServerApp) // то самое использование Accessible
      .flatMap { serverApp =>
        Server.start(8090, serverApp).exitCode // стартанем наш http-сервер
      }
      .provideLayer(applicationLayer)
  }
}