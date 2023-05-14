import sbt.Keys._
import sbt.{Def, _}

object ScalaConventions extends AutoPlugin {
  override def requires = sbt.plugins.JvmPlugin
  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    scalaVersion := Dependencies.scala3Version,
    organization := "com.google.example",
    version := "0.1.0-SNAPSHOT"
  )
}
