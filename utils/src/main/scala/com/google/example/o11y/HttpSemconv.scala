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

import io.opentelemetry.api.common.AttributeKey

object HttpSemconv:
  val httpRoute = AttributeKey.stringKey("http.route")
  val clientAddress = AttributeKey.stringKey("client.address")
  val clientPort = AttributeKey.stringKey("client.port")
  val serverAddress = AttributeKey.stringKey("server.address")
  val serverPort = AttributeKey.longKey("server.port")
  val urlPath = AttributeKey.stringKey("url.path")
  val urlQuery = AttributeKey.stringKey("url.query")
  val urlScheme = AttributeKey.stringKey("url.scheme")
  val httpStatusCode = AttributeKey.longKey("http.response.status_code")