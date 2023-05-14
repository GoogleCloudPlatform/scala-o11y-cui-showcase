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
