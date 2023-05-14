package com.google.example.services.auction

import com.google.example.o11y.cask._
import upickle.default.writeJs

/** Microservice for posting and clearing auctions for items. */
object AuctionServer  extends OtelMainRoutes:
  initialize()
  // TODO - Move this to a database.
  private val dataStore: AuctionDataStore = new LocalAuctionDataStore()

  override def port: Int = 8080
  override def host: String = "0.0.0.0"

  @traced
  @cask.get("/auctions")
  def list() =
    writeJs(dataStore.list())

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
