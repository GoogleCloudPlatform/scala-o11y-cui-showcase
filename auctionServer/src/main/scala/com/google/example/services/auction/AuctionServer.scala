package com.google.example.services.auction


import com.google.example.o11y.{traced, CaskToSlf4jLogger, propagatedHeaders}
import upickle.default.writeJs

/** Microservice for posting and clearing auctions for items. */
object AuctionServer  extends cask.MainRoutes:
  println("Initializing Auction Server.")
  com.google.example.o11y.initializeOpenTelemetry()
  initialize()

  // TODO - Move this to a database.
  private val dataStore: AuctionDataStore = new LocalAuctionDataStore()

  override def port: Int = 8080
  override def host: String = "0.0.0.0"

  @traced
  @cask.get("/auctions")
  def list() =
    println("list()")
    writeJs(dataStore.list())

  @traced
  @cask.get("/auctions/:id")
  def listOne(id: Long) =
    println(s"listOne($id)")
    dataStore.get(id) match
      case None => throw new IllegalArgumentException(s"Auction not found: ${id}")
      case Some(auction) => writeJs(auction)

  @traced
  @cask.postJson("/auctions")
  def add(description: String, minBid: Option[Float] = None) =
    println(s"add($description, $minBid)")
    writeJs(dataStore.add(description, minBid.getOrElse(0f)))

  @traced
  @cask.delete("/auctions/:id")
  def delete(id: Long) =
    println(s"delete(id)")
    dataStore.delete(id) match
      case None => throw new IllegalArgumentException(s"Id not found: ${id}")
      case Some(auction) => writeJs(auction)


  @traced
  @cask.postJson("/auctions/:id/bid")
  def bid(id: Long, bid: Float) =
    println(s"bid($id, $bid)")
    dataStore.bid(id, bid) match
      case None => throw new IllegalArgumentException(s"Auction not found: ${id}")
      case Some(auction) => writeJs(auction)
