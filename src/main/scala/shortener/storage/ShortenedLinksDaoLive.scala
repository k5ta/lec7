package shortener.storage

import shortener.model.error.ShortenerServiceError.ShortenedLinksDaoError
import shortener.model.links._
import zio._

import java.util.concurrent.ConcurrentHashMap

class ShortenedLinksDaoLive(storageController: ConcurrentHashMap[UrlHash, OriginUrl]) extends ShortenedLinksDao {

  override def putIfAbsent(params: ShortenedUrlParams): IO[ShortenedLinksDaoError, OriginUrl] = {
    // получаем значение по ключу или создаем его при отсутствии
    IO.effect(storageController.computeIfAbsent(params.hashed, _ => params.origin))
      .mapError(err => ShortenedLinksDaoError(msg = s"On getting or computing for $params", cause = err))
  }

  override def getByKey(hash: UrlHash): IO[ShortenedLinksDaoError, Option[OriginUrl]] = {
    // получаем ссылку по ее хэшу
    IO.effect(storageController.get(hash))
      .mapBoth(
        err => ShortenedLinksDaoError(msg = s"While getting original for $hash", cause = err),
        result => Option(result) // поскольку ConcurrentHashMap при отсутствии значения вернет null - обернем в Option
      )
  }

  override def removeByKey(hash: UrlHash): IO[ShortenedLinksDaoError, Unit] =
    IO.effect(storageController.remove(hash))
      .mapError(err => ShortenedLinksDaoError(msg = s"While removing for hash $hash", err))
      .unit
}

object ShortenedLinksDaoLive {

  val live: ULayer[Has[ShortenedLinksDao]] =
    ZLayer.succeed(new ShortenedLinksDaoLive(new ConcurrentHashMap[UrlHash, OriginUrl]()))
}
