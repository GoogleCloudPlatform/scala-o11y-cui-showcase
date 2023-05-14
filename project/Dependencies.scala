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
