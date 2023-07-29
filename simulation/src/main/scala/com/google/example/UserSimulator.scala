package com.google.example

import java.util.TimerTask
import java.util.concurrent.Executor

// This is a simulation task run every second.
class UserSimulator(dsl: SimulationDsl, pool: Executor) extends Runnable:
  val rng = java.util.Random()
  def signInAuctionViewer(id: Int): Runnable =
    new Runnable:
      override def run(): Unit =
        System.out.println(s"Simulating user $id")
        // Try unauthenticated, get bounced, auth, then list auctions.
        dsl.listAuctions()
        val auth = dsl.login()
        dsl.listAuctions(Some(auth))

  def simulateAuctionViewers(): Unit =
    for i <- 0 until 20 do
      pool.execute(signInAuctionViewer(i))

  override def run(): Unit =
    System.out.println(s"Simulating User Actions @ ${java.time.LocalDateTime.now()}")
    // Here we load up the executor pool with tasks to perform.
    simulateAuctionViewers()