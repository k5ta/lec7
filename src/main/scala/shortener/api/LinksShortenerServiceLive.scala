package shortener.api

import shortener.api.LinksShortenerServiceLive.RequestCharset
import shortener.logic.LinksManager
import shortener.model.error.ShortenerServiceError._
import shortener.model.links.{OriginUrl, UrlHash}
import zhttp.http._
import zio.{Has, IO, UIO, URLayer}
import zio.console.Console

import java.io.IOException
import java.nio.charset.Charset

// имплементация - нам нужен менеджер ссылок и консолька (на самом деле только ради логирования)
class LinksShortenerServiceLive(manager: LinksManager, console: Console.Service) extends LinksShortenerService {

  private lazy val httpServer = Http.collectZIO[Request] {

    // эндпоинт получения URL по его сокращенному варианту
    case Method.GET -> !! / hashString =>
      manager
        .getByHash(UrlHash(hashString)) // получим из менеджера ссылок оригинальную по хэшу
        .map(params => Response.text(params.origin.rawUrl)) // В идеале здесь нужно редиректить на сайт, но с нашей библиотекой это будет довольно гемморно
        .catchAll {
          // в зависимости от типа ошибки вернем разные статус-коды
          case LinksManagerError(msg, _) => processErrorResponse(msg, Status.NotFound)
          case ShortenedLinksDaoError(msg, _) => processErrorResponse(msg, Status.InternalServerError)
        }

    // создаем сокращенный url по полному - используем POST метод, данные - сайт для сокращения
    case createRequest if createRequest.method == Method.POST && createRequest.path == !! / "create" =>
      createRequest
        .data // получаем переданные данные
        .toByteBuf
        .flatMap { byteData =>
          val rawUrl = byteData.toString(RequestCharset) // получаем их строковое представление
          manager.createFromRaw(OriginUrl(rawUrl)) // и прокидываем в метод создания
        }
        .map(params => Response.text(params.hashed.hash)) // аналогично, упрощения ради вернем просто хэш нашего сокращенного сайта
        .catchAll {
          case LinksManagerError(msg, _) => processErrorResponse(msg, Status.Conflict)
          case ShortenedLinksDaoError(msg, _) => processErrorResponse(msg, Status.InternalServerError)
        }

    // удаление уже существующего сокращенного варианта
    case Method.DELETE -> !! / "delete" / hashString =>
      manager
        .removeByHash(UrlHash(hashString))
        .catchAll(
          _ => IO.succeed(Response.status(Status.InternalServerError))
        ).as(Response.ok)
  }

  def asServerApp: UIO[Http[Any, Throwable, Request, Response]] = UIO.succeed(httpServer)

  private def processErrorResponse(message: String, status: Status): IO[IOException, Response] = {
    // просто залогируем ошибку и прокинем соответствующий ей статус-код
    console.putStrLn(message).as(Response.status(status))
  }
}

object LinksShortenerServiceLive {

  val RequestCharset: Charset = Charset.forName("UTF-8")

  val live: URLayer[Has[LinksManager] with Console, Has[LinksShortenerService]] =
    (new LinksShortenerServiceLive(_, _)).toLayer
}
