package shortener.logic

import shortener.model.error.ShortenerServiceError
import shortener.model.links.{OriginUrl, ShortenedUrlParams, UrlHash}
import zio.IO

// сервис для управления ссылками и их сокращенными вариантами
trait LinksManager {

  def createFromRaw(rawUrl: OriginUrl): IO[ShortenerServiceError, ShortenedUrlParams]

  def getByHash(hash: UrlHash): IO[ShortenerServiceError, ShortenedUrlParams]

  def removeByHash(hash: UrlHash): IO[ShortenerServiceError, Unit]
}
