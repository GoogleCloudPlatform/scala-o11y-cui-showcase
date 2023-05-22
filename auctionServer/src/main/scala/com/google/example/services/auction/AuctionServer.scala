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

package com.google.example.services.auction

import com.google.example.o11y.cask._
import upickle.default.writeJs
import io.opentelemetry.api.common.Attributes

/** Microservice for posting and clearing auctions for items. */
object AuctionServer  extends OtelMainRoutes:
  initialize()
  // TODO - Move this to a database.
  private val dataStore: AuctionDataStore = new LocalAuctionDataStore()
  private val eventLogger = io.opentelemetry.api.events.GlobalEventEmitterProvider.get().get("AuctionServer")

  override def port: Int = 8080
  override def host: String = "0.0.0.0"

  @traced
  @cask.get("/auctions")
  def list() =
    val result = dataStore.list()
    eventLogger.emit(
      "auction.views",
      Attributes.builder()
        .put("items.returned", result.length)
      .build())
    writeJs(result)

  @traced
  @cask.get("/auctions/:id")
  def listOne(id: Long) =
    dataStore.get(id) match
      case None => throw new IllegalArgumentException(s"Auction not found: ${id}")
      case Some(auction) => writeJs(auction)

  @traced
  @cask.postJson("/auctions")
  def add(description: String, minBid: Option[Float] = None) =
    writeJs(dataStore.add(description, minBid.getOrElse(0f)))

  @traced
  @cask.delete("/auctions/:id")
  def delete(id: Long) =
    dataStore.delete(id) match
      case None => throw new IllegalArgumentException(s"Id not found: ${id}")
      case Some(auction) => writeJs(auction)


  @traced
  @cask.postJson("/auctions/:id/bid")
  def bid(id: Long, bid: Float) =
    dataStore.bid(id, bid) match
      case None => throw new IllegalArgumentException(s"Auction not found: ${id}")
      case Some(auction) => writeJs(auction)
