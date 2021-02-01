import java.net.URI

package object utils {
  def formatRelativeUrl(domain: String, url: String): String = {
    val uri = new URI(url)
    if (uri.isAbsolute) {
      url
    } else {
      if (url.startsWith("/")) {
        f"$domain$url"
      } else {
        f"$domain/$url"
      }
    }
  }

  def isFromSite(domain: String, url: String): Boolean = {
    if (url != null) {
      if (url.startsWith(domain)) {
        true
      } else if (url contains f"://$domain") {
        true
      } else if (url contains f".$domain") {
        true
      } else {
        false
      }
    } else {
      false
    }
  }

  def removeFragments(url: String): String = {
    url.split("#").head
  }

  def formatUrlScheme(url: String, https: Boolean): String = {
    var toScheme: String = "https://"
    var fromScheme: String = "http://"
    if (!https) {
      toScheme = "http://"
      fromScheme = "https://"
    }
    if (url.startsWith("http")) {
      url.replace(fromScheme, toScheme)
    } else {
      f"$toScheme$url"
    }
  }

  def formatWwwPrefix(domain: String, url: String, www: Boolean): String = {
    if (!www) {
      url.replace("://www.", "://")
    } else {
      if (url contains f"//$domain") {
        url.replace("://", "://www.")
      } else {
        url
      }
    }
  }

  def formatTrailingSlash(url: String, trailingSlash: Boolean): String = {
    if (trailingSlash) {
      if (url.endsWith("/")) {
        url
      } else {
        f"$url/"
      }
    } else {
      if (url.endsWith("/")) {
        url.stripSuffix("/")
      } else {
        url
      }
    }
  }

  def isASCII(url: String): Boolean = {
    for (c <- url.toCharArray) {
      if (c.toInt > 127) return false
    }
    true
  }
}
