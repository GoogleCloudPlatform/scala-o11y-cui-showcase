package com.google.example

import com.google.example.services.messages.Auction

import java.util.TimerTask
import java.util.concurrent.Executor

// This is a simulation task run every second.
class UserSimulator(dsl: SimulationDsl, pool: Executor) extends Runnable:
  val rng = java.util.Random()
  val userAuthCache = collection.concurrent.TrieMap[Int, String]()
  val userAuctionIdCache = collection.concurrent.TrieMap[Int, Long]()

  // TODO - fun names for users.
  def username(id: Int) = s"user${id}"
  def signInAuctionViewer(id: Int): Runnable = () =>
    System.out.println(s"User $id attempting to view auctions")

    val auth = userAuthCache.getOrElseUpdate(id, dsl.login(username(id)))
    // Try unauthenticated, get bounced, auth, then list auctions.
    dsl.listAuctions(auth) match
      case "Please Login!" =>
        // Forget our auth for next time, and re-run
        userAuthCache.drop(id)
      case result =>
        // Success
        System.out.println(s"User $id got success: $result")

  def postOrDeleteAuction(id: Int): Runnable = () =>
    val auth = userAuthCache.getOrElseUpdate(id, dsl.login(username(id)))
    userAuctionIdCache.get(id) match
      case None =>
        System.out.println(s"User $id attempting to post an auction")
        // TODO - Save the posted auction and delete it after some time.
        val auction = dsl.postAuction(auth, s"User $id's item")
        userAuctionIdCache.put(id, auction.id)
      case _ =>
        System.out.println(s"User $id attempting to delete an auction")
        userAuctionIdCache.remove(id) match
          case Some(auctionId) => dsl.deleteAuction(auth, auctionId)
          case None => ()

  def bidOnAuction(id: Int): Runnable = () =>
    val auth = userAuthCache.getOrElseUpdate(id, dsl.login(username(id)))
    dsl.listAuctions(auth) match
      case "Please Login!" =>
        userAuthCache.drop(id)
      case result =>
        // TODO - Pick an auction and bid on it.
        val auctions = upickle.default.read[Seq[Auction]](result)
        val auction = auctions(rng.nextInt(auctions.length))
        val bid = auction.bids.map(_.amount).maxOption.getOrElse(auction.minBid) + 1f
        System.out.println(s"User $id is bidding ${bid} on auction ${auction.id}")
        dsl.bid(auth, auction.id, bid)

  def simulateAuctionViewers(): Unit =
    for i <- 0 until 20 do
      rng.nextInt(100) match
        // 1% of time users add new auction items
        case n if n < 1 => pool.execute(postOrDeleteAuction(i))
        // 9% of the time users will bid on auctions they see
        case n if n < 10 => pool.execute(bidOnAuction(i))
        // Remainder of users sign in and read auction
        case _ => pool.execute(signInAuctionViewer(i))

  override final def run(): Unit =
    System.out.println(s"Simulating User Actions @ ${java.time.LocalDateTime.now()}")
    // Here we load up the executor pool with tasks to perform.
    simulateAuctionViewers()