package com.google.example

import java.util.concurrent.{Executors, TimeUnit}

object Simulation:
  final def main(args: Array[String]): Unit =
    val userThreads = Executors.newFixedThreadPool(20)
    val dsl = SimulationDsl()
    val users = UserSimulator(dsl, userThreads)
    System.err.println("Spinning up simulation of users.")
    Executors.newScheduledThreadPool(1).scheduleAtFixedRate(users, 0, 1, TimeUnit.SECONDS)
