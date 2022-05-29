package shortener.model.links

// в идеале нужна валидация URL-а, в текущей реализации в данный класс-обертку можно закинуть что угодно
case class OriginUrl(rawUrl: String)
