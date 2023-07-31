package com.google.example

/** This represents actions a fake user can take one the system. */
trait SimulationDsl:
  def login(user: String): String
  def listAuctions(authToken: String): String

  def postAuction(authToken: String, description: String): String

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

      override def postAuction(authToken: String, description: String): String =
        requests.post(
          s"${frontend}/auctions",
          data = ujson.Obj("description" -> description),
          auth = requests.RequestAuth.Bearer(authToken)
        ).text()

