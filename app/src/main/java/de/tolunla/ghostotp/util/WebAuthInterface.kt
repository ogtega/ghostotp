package de.tolunla.ghostotp.util

import android.content.Context
import android.webkit.JavascriptInterface

class WebAuthInterface(private val context: Context) {

  @JavascriptInterface
  fun saveAuth(json: String) {

  }
}