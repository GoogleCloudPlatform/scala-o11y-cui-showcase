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
