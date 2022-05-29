package shortener.api

import zhttp.http.{Http, Request, Response}
import zio.{Accessible, UIO}

// сервис, предоставляющий непосредственно API нашего сервиса сокращения ссылок
trait LinksShortenerService {

  def asServerApp: UIO[Http[Any, Throwable, Request, Response]]
}

object LinksShortenerService extends Accessible[LinksShortenerService]
