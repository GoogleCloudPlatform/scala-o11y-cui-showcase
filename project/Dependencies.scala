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

import sbt._

// Lazy dependency management
object Dependencies {
  val scala3Version = "3.2.2"
  val otelVersion = "1.26.0"
  val slf4jVersion = "2.0.6"
  val logbackVersion = "1.4.6"
  val otel = new {
    val api = "io.opentelemetry" % "opentelemetry-api" % otelVersion
    val sdkLogs = "io.opentelemetry" % "opentelemetry-sdk-logs" % s"${otelVersion}-alpha"
    val sdkAutoconf = "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % s"${otelVersion}-alpha"
    val exporterOtlp = "io.opentelemetry" % "opentelemetry-exporter-otlp" % s"${otelVersion}"
    val exporterOtlpLogs = "io.opentelemetry" % "opentelemetry-exporter-otlp-logs" % s"${otelVersion}-alpha"
    val instrumentationLogback = "io.opentelemetry.instrumentation" % "opentelemetry-logback-appender-1.0" % s"${otelVersion}-alpha"
  }
  val slf4j = new {
    val api = "org.slf4j" % "slf4j-api" % slf4jVersion
    val jul = "org.slf4j" % "jul-to-slf4j" % slf4jVersion
  }
  val logback = new {
    val core =  "ch.qos.logback" % "logback-core" % logbackVersion
    val classic = "ch.qos.logback" % "logback-classic" % logbackVersion
  }
  val lihaoyi = new {
    val cask = "com.lihaoyi" %% "cask" % "0.9.1"
    val requests = "com.lihaoyi" %% "requests" % "0.8.0"
    val upickle = "com.lihaoyi" %% "upickle" % "3.1.0"
  }
}
