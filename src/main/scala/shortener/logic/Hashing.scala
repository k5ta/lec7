package shortener.logic

import shortener.model.links.{OriginUrl, UrlHash}

object Hashing {

  def getHash(origin: OriginUrl): UrlHash = {
    // будем использовать простейшее хеширование - вполне достаточно для наших нужд
    val rawHash = util.hashing.MurmurHash3.stringHash(origin.rawUrl).toHexString
    UrlHash(rawHash)
  }
}
