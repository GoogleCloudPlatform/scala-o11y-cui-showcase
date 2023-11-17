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

import cask.Request
import cask.model.Response.Raw
import cask.router.{RawDecorator, Result}
import com.google.example.o11y.cask.{OtelMainRoutes, cui}
import com.google.example.o11y.requests
import com.google.example.auth.cask.authorized

// This is our "outer gateway" to the microservices.
object MyApplication extends OtelMainRoutes:
  initialize()
  private val rng = java.util.Random()
  private val AuctionServerUrl = sys.env.getOrElse("AUCTION_SERVER", "http://localhost:8080")
  private val AuthServerUrl = sys.env.getOrElse("AUTH_SERVER", "http://localhost:8082")
  private val eventLogger =
    io.opentelemetry.api.events.GlobalEventEmitterProvider.get()
      .get("FrontendServer")
  override def port: Int = 8080
  override def host: String = "0.0.0.0"


  @cui("search")
  @authorized(redirect = Some("/login"))
  @verifyToken()
  @cask.get("/")
  def index() =
    log.debug("Serving index.")
    val text = requests.get(s"${AuctionServerUrl}/auctions").text()
    if rng.nextBoolean()
    then time("readJson2")(ujson.read(text))
    else time("readJson1") {
      Thread.sleep(2000L)
      ujson.read(text)
    }

  @cask.get("/login")
  def login() =
    // TODO - create HTML form login
    "Please Login!"

  @cui("login")
  @cask.postJson("/login")
  def login(username: String, password: String) =
    // TODO - Do something with the response so our token is saved for the whole session.
    val text =
      requests.post(s"${AuthServerUrl}/login_form", data=Map("username"->username, "password"->password)).text()
    text


  @cui("post_auction")
  @authorized()
  @verifyToken()
  @cask.postJson("/auctions")
  def postAuction(description: String, minBid: Option[Float] = None) =
    val text = requests.post(s"${AuctionServerUrl}/auctions",
      data = ujson.Obj("description" -> description, "minBid" -> minBid)
    ).text()
    time("readJson")(ujson.read(text))

  @cui("bid_auction")
  @authorized()
  @verifyToken()
  @cask.postJson("/auctions/:id/bid")
  def bid(id: Long, bid: Float) =
    val text = requests.post(s"${AuctionServerUrl}/auctions/${id}/bid",
      data = ujson.Obj("bid" -> bid)
    ).text()
    // Experiment w/ new read json API.
    time("readJson")(ujson.read(text))

  @cui("delete_auction")
  @authorized()
  @verifyToken()
  @cask.delete("/auctions/:id")
  def delete(id: Long) =
    requests.delete(s"${AuctionServerUrl}/auctions/${id}").text()


  // Verifies user's auth token with the Auth Server.  Any non-OK response is treated as a broken token.
  class verifyToken extends RawDecorator:
    override def wrapFunction(ctx: Request, delegate: Delegate): Result[Raw] =
      requests.get(s"${AuthServerUrl}/check").text() match
        case "Ok" => delegate(Map())
        case _ => Result.Success(cask.Abort(401))