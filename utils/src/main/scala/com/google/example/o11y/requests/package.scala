package com.google.example.o11y

import java.net.HttpCookie
import javax.net.ssl.SSLContext
import scala.collection.mutable
import _root_.requests.{BaseSession,RequestAuth,Cert,Compress}

// Our version of the li haoyi's requests library w/ baked in OpenTelemetry support.
// TODO - Client spans?
package object requests extends BaseSession:
  def cookies = mutable.Map.empty[String, HttpCookie]
  override def headers = BaseSession.defaultHeaders ++ propagatedHeaders()
  def auth = RequestAuth.Empty
  def proxy = null
  def cert: Cert = null
  def sslContext: SSLContext = null
  def maxRedirects: Int = 5
  def persistCookies = false
  def readTimeout: Int = 10 * 1000
  def connectTimeout: Int = 10 * 1000
  def verifySslCerts: Boolean = true
  def autoDecompress: Boolean = true
  def compress: Compress = Compress.None
  def chunkedUpload: Boolean = false
  def check: Boolean = true