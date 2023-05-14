lazy val utils =
  project
    .in(file("utils"))
    .enablePlugins(ScalaConventions)
    .settings(
      libraryDependencies ++= Seq(
        Dependencies.otel.api,
        Dependencies.otel.sdkLogs,
        Dependencies.otel.sdkAutoconf,
        Dependencies.otel.instrumentationLogback,
        Dependencies.slf4j.api,
        Dependencies.slf4j.jul,
        Dependencies.lihaoyi.cask,
        Dependencies.lihaoyi.requests,
        Dependencies.lihaoyi.upickle
      )
    )

lazy val auctionServer =
  project
    .in(file("auctionServer"))
    .enablePlugins(JavaServerAppPackaging, ScalaConventions)
    .settings(
      dockerBaseImage := "openjdk:21-slim",
      dockerExposedPorts += 8080,
      libraryDependencies ++= Seq(
        Dependencies.otel.exporterOtlp,
        Dependencies.otel.exporterOtlpLogs,
        Dependencies.logback.core,
        Dependencies.logback.classic
      )
    )
    .dependsOn(utils)

lazy val root = project
  .in(file("."))
  .enablePlugins(ScalaConventions)
  .dependsOn(utils)
  .aggregate(utils, auctionServer)
  .settings(
    name := "scala-o11y-showcase"
  )
