lazy val utils =
  project
    .in(file("utils"))
    .enablePlugins(ScalaConventions)
    .dependsOnExternal(
      Dependencies.otel.api,
      Dependencies.otel.sdkLogs,
      Dependencies.otel.sdkAutoconf,
      Dependencies.otel.instrumentationLogback,
      Dependencies.slf4j.api,
      Dependencies.slf4j.jul,
      Dependencies.lihaoyi.cask,
      Dependencies.lihaoyi.requests,
      Dependencies.lihaoyi.upickle,
      Dependencies.jwt.upickle,
      Dependencies.otel.sdkTesting % "test",
      Dependencies.assertj.core % "test",
    )

lazy val messages =
  project.in(file("messages"))
  .enablePlugins(ScalaConventions)
  .dependsOnExternal(Dependencies.lihaoyi.upickle)

lazy val auctionServer =
  project
    .in(file("auctionServer"))
    .enablePlugins(DockerConventions)
    .dependsOn(utils, messages)

lazy val authServer =
  project
    .in(file("authServer"))
    .enablePlugins(DockerConventions)
    .dependsOn(utils)

lazy val simulation =
  project
    .in(file("simulation"))
    .enablePlugins(DockerConventions)
    .dependsOn(utils, messages)

lazy val root = project
  .name("scala-o11y-showcase")
  .in(file("."))
  .enablePlugins(DockerConventions)
  .dependsOn(utils, messages)
  .aggregate(utils, auctionServer, authServer, simulation, messages)

ThisBuild / githubWorkflowJavaVersions += JavaSpec.temurin("17")
ThisBuild / crossScalaVersions := Seq((ThisBuild / scalaVersion).value)
ThisBuild / scalaVersion := Dependencies.scala3Version
ThisBuild / githubWorkflowPublish := Nil
