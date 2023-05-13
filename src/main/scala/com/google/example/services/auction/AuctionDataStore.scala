package com.google.example.services.auction

/** Abstraction we use to stub-out different datastores/backends. */
trait AuctionDataStore:
  /** Stores an auction. */
  def add(description: String, minBid: Float): Auction
  /** Deletes an auction, returning its value. */
  def delete(id: Long): Option[Auction]
  /** Returns all live auctions. */
  def get(id: Long): Option[Auction]
  /** Returns all live auctions. */
  def list(): Seq[Auction]
  /** Creates a new bid on a specific auction. */
  def bid(id: Long, bid: Float): Option[Auction]


/** Lame, synchronous in-memory implementation. */
class LocalAuctionDataStore extends AuctionDataStore:
  private var nextId = 1L
  private var auctions = List[Auction](Auction(5000, "seed item", "Josh", 0, Seq()))


  override def add(description: String, minBid: Float): Auction =
    synchronized {
      val auction = Auction(nextId, description, "{unknown}", minBid, List())
      nextId += 1
      auctions = auction :: auctions
      auction
    }
  override def delete(id: Long): Option[Auction] =
    auctions.find(_.id == id) match
      case None => None
      case Some(auction) =>
        synchronized {
          auctions = auctions.filter(_ != auction)
        }
        Some(auction)
  override def list(): Seq[Auction] = auctions
  override def get(id: Long): Option[Auction] = auctions.find(_.id == id)
  override def bid(id: Long, bid: Float): Option[Auction] =
    synchronized {
      auctions.find(_.id == id).map { auction =>
        val b = Bid(auction.bids.length + 1, "{unknown}", bid)
        val updated = auction.copy(bids = auction.bids :+ b)
        auctions = updated :: auctions.filter(_ != auction)
        updated
      }
    }