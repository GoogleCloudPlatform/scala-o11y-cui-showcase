package com.google.example

import java.util.TimerTask
import java.util.concurrent.Executor

// This is a simulation task run every second.
class UserSimulator(dsl: SimulationDsl, pool: Executor) extends Runnable:
  val rng = java.util.Random()
  val userAuthCache = collection.mutable.HashMap[Int, String]()

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
        run()
      case result =>
        // Success
        System.out.println(s"User $id got success: $result")

  def postAuction(id: Int): Runnable = () =>
    System.out.println(s"User $id attempting to post an auction")
    val auth = userAuthCache.getOrElseUpdate(id, dsl.login(username(id)))
    // TODO - Save the posted auction and delete it after some time.
    dsl.postAuction(auth, s"User $id's item")

  def simulateAuctionViewers(): Unit =
    for i <- 0 until 20 do
      rng.nextInt(100) match
        // 1% of time users add new auction items
        case n if n < 1 => pool.execute(postAuction(i))
        // Remainder of users sign in and read auction
        case _ => pool.execute(signInAuctionViewer(i))

  override final def run(): Unit =
    System.out.println(s"Simulating User Actions @ ${java.time.LocalDateTime.now()}")
    // Here we load up the executor pool with tasks to perform.
    simulateAuctionViewers()