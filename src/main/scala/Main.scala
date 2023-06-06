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

import com.google.example.o11y.cask.{cui, OtelMainRoutes}
import com.google.example.o11y.requests
import com.google.example.auth.cask.authorized

// This is our "outer gateway" to the microservices.
object MyApplication extends OtelMainRoutes:
  initialize()
  private val AuctionServerUrl = sys.env.getOrElse("AUCTION_SERVER", "http://localhost:8080")
  private val AuthServerUrl = sys.env.getOrElse("AUTH_SERVER", "http://localhost:8082")
  override def port: Int = 8080
  override def host: String = "0.0.0.0"

  @cui("search")
  @authorized(roles=Seq("read"), redirect = Some("/login"))
  @cask.get("/")
  def index() =
    log.debug("Serving index.")
    requests.get(s"${AuctionServerUrl}/auctions").data.array

  @cask.get("/login")
  def login() =
    // TODO - create HTML form login
    "Please Login!"

  @cask.postJson("/login")
  def login(username: String, password: String) =
    // TODO - Do something with the response so our token is saved for the whole session.
    requests.post(s"${AuthServerUrl}/login_form", data=Map("username"->username, "password"->password)).text()



