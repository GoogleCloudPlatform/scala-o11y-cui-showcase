package com.google.example.services.auction

import com.google.example.o11y.{traced, CaskToSlf4jLogger, propagatedHeaders}
import upickle.default.writeJs

/** Microservice for posting and clearing auctions for items. */
object AuctionServer  extends cask.MainRoutes:
  com.google.example.o11y.initializeOpenTelemetry()
  initialize()

  // TODO - Move this to a database.
  private var nextId = 1L
  private var auctions = List[Auction](Auction(5000, "seed item", "Josh", 0, Seq()))

  @traced
  @cask.get("/auctions")
  def list() =
    println("list()")
    writeJs(auctions)

  @traced
  @cask.get("/auctions/:id")
  def listOne(id: Long) =
    println(s"listOne($id)")
    auctions.find(_.id == id) match
      case None => throw new IllegalArgumentException(s"Auction not found: ${id}")
      case Some(auction) => writeJs(auction)

  @traced
  @cask.postJson("/auctions")
  def add(description: String, minBid: Option[Float] = None) =
    println(s"add($description, $minBid)")
    val auction = Auction(nextId, description, "{unknown}", minBid.getOrElse(0), List())
    // TODO - move this a database
    nextId += 1
    auctions = auction :: auctions
    writeJs(auction)

  @traced
  @cask.delete("/auctions/:id")
  def delete(id: Long) =
    println(s"delete(id)")
    auctions.find(_.id == id) match
      case None => throw new IllegalArgumentException(s"Id not found: ${id}")
      case Some(auction) =>
        auctions = auctions.filter(_ != auction)
        writeJs(auction)


  @traced
  @cask.postJson("/auctions/:id/bid")
  def bid(id: Long, bid: Float) =
    println(s"bid($id, $bid)")
    auctions.find(_.id == id) match
      case None => throw new IllegalArgumentException(s"Auction not found: ${id}")
      case Some(auction) =>
        val b = Bid(auction.bids.length+1, "{unknown}", bid)
        val updated = auction.copy(bids = auction.bids :+ b)
        auctions = updated :: auctions.filter(_ != auction)
        writeJs(updated)
