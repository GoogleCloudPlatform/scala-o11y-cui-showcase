package com.google.example

import com.google.example.services.messages.{Auction,Bid}

/** This represents actions a fake user can take one the system. */
trait SimulationDsl:
  def login(user: String): String
  def listAuctions(authToken: String): String
  def postAuction(authToken: String, description: String): Auction
  def deleteAuction(authToken:String, auctionId: Long): String
  def bid(authToken: String, auction: Long, bid: Float): String

object SimulationDsl:
  def apply(): SimulationDsl =
    val frontend = sys.env.getOrElse("FRONTEND_SERVER", "http://localhost:8081")
    new SimulationDsl:
      override def login(user: String): String =
        // TODO - multiple fake users
        requests.post(
          s"${frontend}/login",
          data =
            ujson.Obj("username"->user,"password" -> "password")
        ).text()
      override def listAuctions(authToken: String): String =
        val result = requests.get(
              s"${frontend}/",
              auth = requests.RequestAuth.Bearer(authToken)
            ).text()
        result

      override def postAuction(authToken: String, description: String): Auction =
        import upickle.default.*
        val result = requests.post(
          s"${frontend}/auctions",
          data = ujson.Obj("description" -> description),
          auth = requests.RequestAuth.Bearer(authToken)
        ).text()
        read[Auction](result)

      override def bid(authToken: String, auction: Long, bid: Float): String =
        requests.post(s"${frontend}/auctions/${auction}/bid",
          data = ujson.Obj("bid" -> bid),
          auth = requests.RequestAuth.Bearer(authToken)
        ).text()

      override def deleteAuction(authToken:String, auctionId: Long): String =
        requests.delete(s"${frontend}/auctions/${auctionId}",
          auth = requests.RequestAuth.Bearer(authToken)
        ).text()

