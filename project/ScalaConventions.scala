import sbt.Keys._
import sbt.{Def, _}

object ScalaConventions extends AutoPlugin {
  override def requires = sbt.plugins.JvmPlugin
  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    scalaVersion := Dependencies.scala3Version,
    organization := "com.google.example",
    version := "0.1.0-SNAPSHOT",
    organizationName := "Google",
    startYear := Some(2023),
    licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))
  )
}
