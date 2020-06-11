package com.doi.garasi.postinterceptor

import android.net.Uri
import android.webkit.WebResourceRequest
import com.doi.garasi.util.data.orFalse

class WriteHandlingWebResourceRequest(private val originalWebResourceRequest: WebResourceRequest?, val ajaxData: String?, uri: Uri?) : WebResourceRequest {

  private var uri: Uri? = null

  init {
    if (uri != null) {
      this.uri = uri
    } else {
      this.uri = originalWebResourceRequest?.url
    }
  }

  override fun getUrl(): Uri? = uri

  override fun isForMainFrame(): Boolean = originalWebResourceRequest?.isForMainFrame.orFalse()

  override fun isRedirect(): Boolean = throw UnsupportedOperationException()

  override fun hasGesture(): Boolean = originalWebResourceRequest?.hasGesture().orFalse()

  override fun getMethod(): String? = originalWebResourceRequest?.method

  override fun getRequestHeaders(): Map<String, String>? = originalWebResourceRequest?.requestHeaders

  fun hasAjaxData(): Boolean = ajaxData != null
}