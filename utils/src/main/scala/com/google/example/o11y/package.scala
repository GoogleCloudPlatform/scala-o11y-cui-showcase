package com.google.example.o11y

import io.opentelemetry.api.baggage.Baggage
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.{Span, SpanKind, StatusCode}
import io.opentelemetry.api.{GlobalOpenTelemetry, OpenTelemetry}
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.{TextMapGetter, TextMapSetter}
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import io.opentelemetry.sdk.logs.{LogRecordProcessor, ReadWriteLogRecord}
import io.opentelemetry.sdk.trace.{ReadWriteSpan, ReadableSpan, SpanProcessor}
import org.slf4j.bridge.SLF4JBridgeHandler

import scala.collection.mutable
import scala.jdk.CollectionConverters.IterableHasAsJava
import scala.util.Using

object HttpSemconv:
  val httpRoute = AttributeKey.stringKey("http.route")
  val clientAddress = AttributeKey.stringKey("client.address")
  val clientPort = AttributeKey.stringKey("client.port")
  val serverAddress = AttributeKey.stringKey("server.address")
  val serverPort = AttributeKey.longKey("server.port")
  val urlPath = AttributeKey.stringKey("url.path")
  val urlQuery = AttributeKey.stringKey("url.query")
  val urlScheme = AttributeKey.stringKey("url.scheme")
  val httpStatusCode = AttributeKey.longKey("http.response.status_code")

// For use w/ requests library
def propagatedHeaders(): Iterable[(String, String)] =
  val result = collection.mutable.Map[String, String]()
  GlobalOpenTelemetry.getPropagators.getTextMapPropagator.inject(Context.current(), result, (c, k, v) => c.put(k,v))
  result

/** Cask wrapper that adds CUI to baggage for remaining processing */
class cui(val id: String) extends cask.RawDecorator:
  def wrapFunction(ctx: cask.Request, delegate: Delegate) =
    // Insert this CUI into baggage.
    val scope = Baggage.fromContext(Context.current()).toBuilder
      .put("cui", id)
      .build().makeCurrent()
    try delegate(Map())
    finally scope.close()


/** Cask wrapper that does context propagation and tracing */
class traced extends cask.RawDecorator:
  val tracer = GlobalOpenTelemetry.getTracer("cask")
  def wrapFunction(ctx: cask.Request, delegate: Delegate) =
    Using.Manager { use =>
      use(GlobalOpenTelemetry.getPropagators.getTextMapPropagator.extract(Context.current(), ctx, CaskTextMapGetter).makeCurrent())
      val spanBuilder =
        tracer.spanBuilder(spanName(ctx))
          .setSpanKind(SpanKind.SERVER)
          .setAttribute(HttpSemconv.clientAddress, ctx.exchange.getConnection.getPeerAddress.toString)
          .setAttribute(HttpSemconv.serverAddress, ctx.exchange.getHostName)
          .setAttribute(HttpSemconv.serverPort, ctx.exchange.getHostPort.toLong)
          .setAttribute(HttpSemconv.urlPath, ctx.exchange.getRequestPath)
          .setAttribute(HttpSemconv.urlQuery, ctx.exchange.getQueryString)
          .setAttribute(HttpSemconv.urlScheme, ctx.exchange.getRequestScheme)
      val span: Span = spanBuilder.startSpan()
      use(span.makeCurrent())
      try
        import cask.router.Result.*
        delegate(Map()) match
          case s: Success[cask.Response.Raw] =>
            span.setAttribute(HttpSemconv.httpStatusCode, s.value.statusCode)
            span.setStatus(StatusCode.OK)
            s
          case e: Error =>
            span.setStatus(StatusCode.ERROR)
            e match
              case Error.Exception(e) => span.recordException(e)
              case _ => // Ignore
            e
      finally
        span.end()
    }.get


  private def spanName(ctx: cask.Request): String =
    s"${ctx.exchange.getRequestMethod.toString} ${ctx.exchange.getRequestURI}"


object CuiKeys:
  val cuiKey = AttributeKey.stringKey("cui")

/** A log processor that appends CUI attributes from baggage to logs. */
object CuiLogRecordProcessor extends LogRecordProcessor:
  override def onEmit(context: Context, logRecord: ReadWriteLogRecord): Unit =
    Baggage.fromContext(context).getEntryValue(CuiKeys.cuiKey.getKey) match
      case null =>
      case value =>
        logRecord.setAttribute(CuiKeys.cuiKey, value)

object CuiSpanProcessor extends SpanProcessor:
  override def isStartRequired: Boolean = true
  override def isEndRequired: Boolean = false
  override def onEnd(span: ReadableSpan): Unit = ()
  override def onStart(parentContext: Context, span: ReadWriteSpan): Unit =
    Baggage.fromContext(parentContext).getEntryValue(CuiKeys.cuiKey.getKey) match
      case null =>
      case value => span.setAttribute(CuiKeys.cuiKey, value)


/** Interaction between CASK and OTEL propagation. */
object CaskTextMapGetter extends TextMapGetter[cask.Request]:
  override def keys(r: cask.Request): java.lang.Iterable[String] = r.headers.keys.asJava
  override def get(r: cask.Request, key: String): String =
    val optResult = for
      values <- r.headers.get(key)
      head <- values.headOption
    yield head
    optResult.orNull

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


/** Helper method to initialize open telemetry for OTLP export. */
def initializeOpenTelemetry(): Unit =
  println("Starting OpenTelemetry!")
  AutoConfiguredOpenTelemetrySdk.builder()
    .addPropertiesSupplier(() => java.util.Map.of("otel.logs.exporter", "otlp"))
    .addLoggerProviderCustomizer((provider, config) => provider.addLogRecordProcessor(CuiLogRecordProcessor))
    .addTracerProviderCustomizer((provider, config) => provider.addSpanProcessor(CuiSpanProcessor))
    .setResultAsGlobal(true)
    .build()
  // Hijack java.util.logging
  SLF4JBridgeHandler.removeHandlersForRootLogger()
  SLF4JBridgeHandler.install()