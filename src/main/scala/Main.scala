
import com.google.example.o11y.{traced, cui, CaskToSlf4jLogger, propagatedHeaders}

// This is our "outer gateway" to the microservices.
object MyApplication extends cask.MainRoutes:
  com.google.example.o11y.initializeOpenTelemetry()
  initialize()

  override val log = CaskToSlf4jLogger()
  private val AuctionServerUrl = sys.env.getOrElse("AUCTION_SERVER", "http://localhost:8080")
  override def port: Int = 8081
  override def host: String = "0.0.0.0"

  @cui("search")
  @traced
  @cask.get("/")
  def index() =
    log.debug("Serving index.")

    // TODO  - create client span
    requests.get(s"${AuctionServerUrl}/auctions", headers=propagatedHeaders()).data.array



