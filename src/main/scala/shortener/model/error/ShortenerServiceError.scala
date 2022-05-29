package shortener.model.error

// абстрактный класс для ошибок, ниже - конкретные варианты в зависимости от сервиса
sealed abstract class ShortenerServiceError(msg: String, cause: Throwable) extends Exception(msg, cause)

object ShortenerServiceError {

  case class ShortenedLinksDaoError(msg: String, cause: Throwable) extends ShortenerServiceError(msg, cause)

  case class LinksManagerError(msg: String, cause: Option[Throwable]) extends ShortenerServiceError(msg, cause.orNull)
}
