val scala3Version = "3.2.2"
val otelVersion = "1.26.0"
val slf4jVersion = "2.0.6"
val logbackVersion = "1.4.6"


lazy val root = project
  .in(file("."))
  .settings(
    name := "scala-playground",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test,
    libraryDependencies ++= Seq(
      // OTEL Dependencies
      "io.opentelemetry" % "opentelemetry-api" % otelVersion,
      "io.opentelemetry" % "opentelemetry-sdk-logs" % s"${otelVersion}-alpha",
      "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % s"${otelVersion}-alpha",
      "io.opentelemetry" % "opentelemetry-exporter-otlp" % s"${otelVersion}",
      "io.opentelemetry" % "opentelemetry-exporter-otlp-logs" % s"${otelVersion}-alpha",
      "io.opentelemetry.instrumentation" % "opentelemetry-logback-appender-1.0" % s"${otelVersion}-alpha",
      // SLF4J dependencies
      "org.slf4j" % "slf4j-api" % slf4jVersion,
      "org.slf4j" % "jul-to-slf4j" % slf4jVersion,
      "ch.qos.logback" % "logback-core" % logbackVersion,
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      // Web server framework
      "com.lihaoyi" %% "cask" % "0.9.1",
      // Client Library
      "com.lihaoyi" %% "requests" % "0.8.0",
      // Serialization
      "com.lihaoyi" %% "upickle" % "3.1.0"
    )
  )
