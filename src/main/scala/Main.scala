
import com.google.example.o11y.{traced, CaskToSlf4jLogger, propagatedHeaders}

//object MyApplication extends cask.MainRoutes:
//  com.google.example.o11y.initializeOpenTelemetry()
//  initialize()
//
//  override val log = CaskToSlf4jLogger()
//
//  @traced
//  @cask.get("/")
//  def index() =
//    log.debug("Serving index.")
//    s"Hello, World!\n${io.opentelemetry.api.trace.Span.current()}\n"
//
//  @traced
//  @cask.get("/error")
//  def bad() =
//    throw new RuntimeException("O NOES")
//
//  @traced
//  @cask.get("/redirected")
//  def downstream() =
//    // TODO - We need to wrap requests so we can create client-side spans.
//    requests.get("http://localhost:8080/", headers=propagatedHeaders()).data.array


// TODO - test w/ OTEL_EXPORTER_OTLP_ENDPOINT or otel.exporter.otlp.endpoint


