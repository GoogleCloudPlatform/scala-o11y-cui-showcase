# Observability Utils for Scala

This module takes the [OpenTelemetry Java SDK](https://github.com/open-telemetry/open-telemetry-java) and
simplifies it for Scala usage with a few libraries.

## Baseline

The `com.google.example.o11y` package provides a few key components:

- An `initializeOpenTelemetry()` method that registers necessary CUI processors and sets up logback / slf4j to demonstrate the logging features.
- A  set of extensions to OpenTelemtery to support CUI tracking (customer user interaction).
  - Helper method `injectCuiNow("id")` which will ensure a CUI is attached to `Baggage` and propagated
  - A `LogRecordProcessor` which attaches CUI from `Baggage` to all generated logs
  - A `SpanProcessor` which attaches CUI from `Baggage` to all generated spans
- A `HttpSemconv`helper object defining HTTP semantic convention attribute keys.

## Cask

The `com.google.example.o11y.cask` package provides three main components:

1. An `OtelMainRoutes` class that replaces `cask.MainRoutes` ensuring OpenTelemetry is initialized appropriately and that logs are sent from Cask to slf4j.
2. A `@traced` annotation that can be added to *any* Cask route to ensure a Span is created when handling that route.
   This annotation should abide by HTTP semantic conventions in OpenTelemetry as of version 1.21
4. A `@cui` annotation that can be used to annotate a particular entry point with a CUI.

Example:

```scala
object MyApplication extends OtelMainRoutes:
  initialize()

  @cui("my-application-browse")
  @traced
  @cask.Get("/")
  def index() = "Hello, World!"
```

## Requests

The `com.google.example.o11y.requests` package *replaces* the Requests library wholesale.  This is
because that library does not provide hooks to allow us to inject spans or context on outgoing HTTP requests.

Instead, you should be able to replace your usage of the Requests library with this import:

```scala
import com.google.example.o11y.requests

requests.get("http://localhost:8080")  // This will include w3c trace/baggage headers
```
