lazy val utils =
  project
    .in(file("utils"))
    .enablePlugins(ScalaConventions)
    .settings(
      libraryDependencies ++= Seq(
        Dependencies.otel.api,
        Dependencies.otel.sdkLogs,
        Dependencies.otel.sdkTesting % "test",
        Dependencies.assertj.core % "test",
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
    .enablePlugins(DockerConventions)
    .dependsOn(utils)

lazy val root = project
  .in(file("."))
  .enablePlugins(DockerConventions)
  .dependsOn(utils)
  .aggregate(utils, auctionServer)
  .settings(
    name := "scala-o11y-showcase"
  )

ThisBuild / githubWorkflowJavaVersions += JavaSpec.temurin("17")
ThisBuild / crossScalaVersions := Seq((ThisBuild / scalaVersion).value)
ThisBuild / scalaVersion := Dependencies.scala3Version
ThisBuild / githubWorkflowPublish := Nil