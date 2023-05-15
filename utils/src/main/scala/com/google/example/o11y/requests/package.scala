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