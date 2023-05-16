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
