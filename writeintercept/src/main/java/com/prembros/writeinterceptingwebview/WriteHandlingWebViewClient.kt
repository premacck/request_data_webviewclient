package com.doi.garasi.postinterceptor

import android.content.Context
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.doi.garasi.base.constants.EMPTY_STRING
import com.doi.garasi.postinterceptor.AjaxInterceptJavascriptInterface.Companion.enableIntercept
import com.doi.garasi.postinterceptor.Utils.consumeInputStream
import com.doi.garasi.util.data.orFalse
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.*

open class WriteHandlingWebViewClient(private val webView: WebView) : WebViewClient() {

  private val ajaxRequestContents: MutableMap<String, String> = HashMap()

  companion object {
    private const val MARKER = "AJAXINTERCEPT"
  }

  /**
   * Remember to call this function when initializing child classes
   */
  protected fun initParentWebViewClientClass() {
    webView.addJavascriptInterface(AjaxInterceptJavascriptInterface(this), "interception")
  }

  /**
   * This here is the "fixed" shouldInterceptRequest method that you should override.
   *  It receives a WriteHandlingWebResourceRequest instead of a WebResourceRequest.
   */
  open fun shouldInterceptRequest(view: WebView?, request: WriteHandlingWebResourceRequest?): WebResourceResponse? = null

  override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
    var requestBody: String? = null
    var uri = request?.url
    if (isAjaxRequest(request)) {
      requestBody = getRequestBody(request)
      uri = getOriginalRequestUri(request, MARKER)
    }
    val webResourceResponse: WebResourceResponse? = shouldInterceptRequest(view, WriteHandlingWebResourceRequest(request, requestBody, uri))
    return if (webResourceResponse == null) {
      webResourceResponse
    } else {
      injectIntercept(webResourceResponse, view?.context)
    }
  }

  fun addAjaxRequest(id: String, body: String) {
    ajaxRequestContents[id] = body
  }

  private fun getRequestBody(request: WebResourceRequest?): String? = getAjaxRequestBodyByID(getAjaxRequestID(request))

  private fun isAjaxRequest(request: WebResourceRequest?): Boolean = request?.url?.toString()?.contains(MARKER).orFalse()

  private fun getUrlSegments(request: WebResourceRequest?, divider: String): Array<String> = request?.url?.toString()?.split(divider)?.toTypedArray() ?: Array(0) { EMPTY_STRING }

  private fun getAjaxRequestID(request: WebResourceRequest?): String = getUrlSegments(request, MARKER)[1]

  private fun getOriginalRequestUri(request: WebResourceRequest?, marker: String): Uri = getUrlSegments(request, marker).getOrNull(0)?.let { Uri.parse(it) } ?: Uri.EMPTY

  private fun getAjaxRequestBodyByID(requestID: String): String? = ajaxRequestContents[requestID].also {
    ajaxRequestContents.remove(requestID)
  }

  private fun injectIntercept(response: WebResourceResponse, context: Context?): WebResourceResponse {
    val encoding = response.encoding
    val mime = response.mimeType
    val responseData = response.data
    val injectedResponseData = injectInterceptToStream(context, responseData, mime, encoding)
    return WebResourceResponse(mime, encoding, injectedResponseData)
  }

  private fun injectInterceptToStream(context: Context?, `is`: InputStream?, mime: String?, charset: String?): InputStream = try {
    var pageContents = consumeInputStream(`is`)
    if (mime == "text/html") {
      pageContents = enableIntercept(context, pageContents).toByteArray(charset(charset.orEmpty()))
    }
    ByteArrayInputStream(pageContents)
  } catch (e: Exception) {
    throw RuntimeException(e.message)
  }
}