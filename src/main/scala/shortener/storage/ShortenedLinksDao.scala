package shortener.storage

import shortener.model.error.ShortenerServiceError.ShortenedLinksDaoError
import shortener.model.links._
import zio.IO

// сервис хранилки сокращенных ссылок - содержит информацию о захешированных и оригинальных вариантах URL
trait ShortenedLinksDao {

  def putIfAbsent(params: ShortenedUrlParams): IO[ShortenedLinksDaoError, OriginUrl]

  def getByKey(hash: UrlHash): IO[ShortenedLinksDaoError, Option[OriginUrl]]

  def removeByKey(hash: UrlHash): IO[ShortenedLinksDaoError, Unit]
}
