import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.{dockerBaseImage, dockerExposedPorts}
import com.typesafe.sbt.packager.archetypes.JavaServerAppPackaging
import sbt.Keys._
import sbt.{Def, _}

object DockerConventions extends AutoPlugin {
  override def requires = ScalaConventions && JavaServerAppPackaging

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    dockerBaseImage := "openjdk:21-slim",
    dockerExposedPorts += 8080,
    libraryDependencies ++= Seq(
      Dependencies.otel.exporterOtlp,
      Dependencies.otel.exporterOtlpLogs,
      Dependencies.logback.core,
      Dependencies.logback.classic
    )
  )
}
