/*
 * Copyright 2023 Google
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.example.o11y

import java.net.HttpCookie
import javax.net.ssl.SSLContext
import scala.collection.mutable
import _root_.requests.{BaseSession, Cert, Compress, Request, RequestAuth, RequestBlob, Requester, Response}
import com.google.example.auth.Jwt
import io.opentelemetry.context.Context
import com.google.example.o11y.HttpSemconv
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.SpanKind

import scala.util.Using

class O11yRequester(verb: String, sess: BaseSession) extends Requester(verb, sess):
  override def apply(url: String,
                     auth: RequestAuth = sess.auth,
                     params: Iterable[(String, String)] = Nil,
                     headers: Iterable[(String, String)] = Nil,
                     data: RequestBlob = RequestBlob.EmptyRequestBlob,
                     readTimeout: Int = sess.readTimeout,
                     connectTimeout: Int = sess.connectTimeout,
                     proxy: (String, Int) = sess.proxy,
                     cert: Cert = sess.cert,
                     sslContext: SSLContext = sess.sslContext,
                     cookies: Map[String, HttpCookie] = Map(),
                     cookieValues: Map[String, String] = Map(),
                     maxRedirects: Int = sess.maxRedirects,
                     verifySslCerts: Boolean = sess.verifySslCerts,
                     autoDecompress: Boolean = sess.autoDecompress,
                     compress: Compress = sess.compress,
                     keepAlive: Boolean = true,
                     check: Boolean = sess.check,
                     chunkedUpload: Boolean = sess.chunkedUpload): Response =
    // Create an OpenTelemetry client span here.
    val span = GlobalOpenTelemetry.get().getTracer("requests")
      .spanBuilder(s"$verb $url")
      .setSpanKind(SpanKind.CLIENT)
      .startSpan()
    Using(span.makeCurrent()) { _ =>
      try super.apply(url, auth, params, headers, data, readTimeout, connectTimeout, proxy, cert, sslContext, cookies, cookieValues, maxRedirects, verifySslCerts, autoDecompress, compress, keepAlive, check, chunkedUpload)
      catch
        case e: Exception =>
          span.recordException(e)
          throw e
      finally span.end()
    }.get


// Our version of the li haoyi's requests library w/ baked in OpenTelemetry support.
// TODO - Client spans?
package object requests extends BaseSession:
  def cookies = mutable.Map.empty[String, HttpCookie]
  // Pull headers from context for o11y, if they exist.
  override def headers = BaseSession.defaultHeaders ++ propagatedHeaders()
  // pull auth from context if it exists.
  override def auth =
    Jwt.tokenFromContext(Context.current) match
      case Some(token) => RequestAuth.Bearer(token)
      case None => RequestAuth.Empty
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


  override lazy val get = O11yRequester("GET", this)
  override lazy val post = O11yRequester("POST", this)
  override lazy val put = O11yRequester("PUT", this)
  override lazy val delete = O11yRequester("DELETE", this)
  override lazy val head = O11yRequester("HEAD", this)
  override lazy val options = O11yRequester("OPTIONS", this)
  // unofficial
  override lazy val patch = O11yRequester("PATCH", this)