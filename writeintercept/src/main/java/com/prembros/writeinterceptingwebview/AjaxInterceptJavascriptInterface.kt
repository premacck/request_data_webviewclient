package com.doi.garasi.postinterceptor

import android.content.Context
import android.webkit.JavascriptInterface
import org.jsoup.Jsoup
import java.io.IOException

class AjaxInterceptJavascriptInterface(private val webViewClient: WriteHandlingWebViewClient?) {

  @JavascriptInterface fun customAjax(ID: String, body: String) {
    webViewClient?.addAjaxRequest(ID, body)
  }

  companion object {
    private var interceptHeader: String? = null
    @JvmStatic @Throws(IOException::class) fun enableIntercept(context: Context?, data: ByteArray?): String {
      if (interceptHeader == null) {
        interceptHeader = context?.assets?.open("interceptHeader.html")?.let { String(Utils.consumeInputStream(it)) }
      }
      val doc = Jsoup.parse(String(data!!))
      doc.outputSettings().prettyPrint(true)

      // Prefix every script to capture submits
      // Make sure our interception is the first element in the
      // header
      val element = doc.getElementsByTag("head")
      if (element.size > 0) {
        element[0].prepend(interceptHeader)
      }
      return doc.toString()
    }
  }
}