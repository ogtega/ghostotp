package de.tolunla.ghostotp.util

import android.content.Context
import android.util.Log
import android.webkit.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class WebAuthAppInterface(private val context: Context) {

  @JavascriptInterface
  fun saveAuth(json: String) {
    Log.d("JS", json)
  }

  class CORSViewClient : WebViewClient() {
    private val formatter: SimpleDateFormat = SimpleDateFormat("E, dd MMM yyyy kk:mm:ss", Locale.US)

    private fun buildCORSResponse(): WebResourceResponse {
      val date = Date()
      val dateString: String = formatter.format(date)

      val headers = HashMap<String, String>()

      headers["Connection"] = "keep-alive"
      headers["Content-Type"] = "text/plain;charset=UTF-8"
      headers["Date"] = "$dateString GMT"
      headers["Server"] = "nginx/1.17.3"
      headers["Access-Control-Allow-Origin"] = "file://"
      headers["Access-Control-Allow-Methods"] = "GET, POST, DELETE, PUT, OPTIONS"
      headers["Access-Control-Max-Age"] = "600"
      headers["Access-Control-Allow-Credentials"] = "true"
      headers["Access-Control-Allow-Headers"] = "accept, authorization, Content-Type"

      return WebResourceResponse("text/plain", "UTF-8", 200, "OK", headers, null)
    }

    override fun shouldInterceptRequest(view: WebView?,
      request: WebResourceRequest?): WebResourceResponse? {

      request?.let {
        if (it.method == "OPTIONS") {
          return buildCORSResponse()
        }

        if (it.method == "POST" && it.url.toString().contains("login")) {
          it.requestHeaders["Referer"] = "https://steamcommunity.com/mobilelogin?oauth_client_id=DE45CD61&oauth_scope=read_profile%20write_profile%20read_client%20write_client"
          return super.shouldInterceptRequest(view, it)
        }
      }

      return null
    }
  }
}