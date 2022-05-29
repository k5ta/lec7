package shortener.logic

import shortener.model.error.ShortenerServiceError
import shortener.model.error.ShortenerServiceError.LinksManagerError
import shortener.model.links.{OriginUrl, ShortenedUrlParams, UrlHash}
import shortener.storage.ShortenedLinksDao
import zio._

class LinksManagerLive(linksDao: ShortenedLinksDao) extends LinksManager {

  override def createFromRaw(rawUrl: OriginUrl): IO[ShortenerServiceError, ShortenedUrlParams] = {
    // получим хэш и создадим инстанс полной информации о ссылке (сама ссылка и ее хэш)
    val hashedUrl = Hashing.getHash(rawUrl)
    val shortenedUrlParams = ShortenedUrlParams(rawUrl, hashedUrl)

    for {
      // добавим в хранилку если нет, или вернем уже хранящуюся по хэшу ссылку
      storedUrl <- linksDao.putIfAbsent(shortenedUrlParams)
      storedRawUrl = Option.when(storedUrl == rawUrl)(storedUrl)

      _ <-
        IO // если хранится отличная от создаваемой ссылки - что-то пошло не так (наш хэш идемпотентен)
          .fromOption(storedRawUrl)
          .orElseFail(
            LinksManagerError(msg = s"Storage already contains hash with url $storedUrl, expects $rawUrl", cause = None)
          )
    } yield shortenedUrlParams
  }

  override def getByHash(hash: UrlHash): IO[ShortenerServiceError, ShortenedUrlParams] =
    for {
      storedUrl <- linksDao.getByKey(hash) // получим ссылку по ее хэшу

      result <-
        IO
          .fromOption(storedUrl) // если ссылки по хэшу нет - кинем ошибку
          .mapBoth(
            _ => LinksManagerError(msg = s"Storage doesn't contains url for hash $hash", cause = None),
            url => ShortenedUrlParams(url, hash)
          )
    } yield result

  override def removeByHash(hash: UrlHash): IO[ShortenerServiceError, Unit] = linksDao.removeByKey(hash)
}

object LinksManagerLive {

  val live: URLayer[Has[ShortenedLinksDao], Has[LinksManager]] = (new LinksManagerLive(_)).toLayer
}
