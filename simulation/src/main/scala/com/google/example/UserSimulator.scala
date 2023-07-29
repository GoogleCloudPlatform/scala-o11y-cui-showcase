package com.google.example

import java.util.TimerTask
import java.util.concurrent.Executor

// This is a simulation task run every second.
class UserSimulator(dsl: SimulationDsl, pool: Executor) extends Runnable:
  val rng = java.util.Random()
  val userAuthCache = collection.mutable.HashMap[Int, String]()
  def signInAuctionViewer(id: Int): Runnable =
    new Runnable:
      override final def run(): Unit =
        System.out.println(s"User $id attempting to view auctions")
        val auth = userAuthCache.getOrElseUpdate(id, dsl.login())
        // Try unauthenticated, get bounced, auth, then list auctions.
        dsl.listAuctions(auth) match
          case "Please Login!" =>
            // Forget our auth for next time, and re-run
            userAuthCache.drop(id)
            run()
          case result =>
            // Success
            System.out.println(s"User $id got success: $result")

  def simulateAuctionViewers(): Unit =
    for i <- 0 until 20 do
      pool.execute(signInAuctionViewer(i))

  override final def run(): Unit =
    System.out.println(s"Simulating User Actions @ ${java.time.LocalDateTime.now()}")
    // Here we load up the executor pool with tasks to perform.
    simulateAuctionViewers()