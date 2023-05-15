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

package com.google.example.o11y.cask

/** Cask logger that delegates to slf4j. */
class CaskToSlf4jLogger extends cask.util.Logger:
  import sourcecode.{File, Line, Text}
  private val logger = org.slf4j.LoggerFactory.getLogger("cask")
  override def debug(t: Text[Any])(implicit f: File, line: Line): Unit =
    logger.atDebug()
      .addKeyValue("source.line", line.value.toString)
      .addKeyValue("source.file", f.value.split("/").last)
      .addKeyValue("source.expression", t.source)
      .log(pprint.apply(t.value).plainText)
  override def exception(t: Throwable): Unit =
    logger.atError()
      .setCause(t)
      .log()
