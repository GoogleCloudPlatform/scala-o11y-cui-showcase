package com.google.example

/** This represents actions a fake user can take one the system. */
trait SimulationDsl:
  def login(): String
  def listAuctions(auth: Option[String] = None): Unit


object SimulationDsl:
  def apply(): SimulationDsl =
    val frontend = sys.env.getOrElse("FRONTEND_SERVER", "http://localhost:8081")
    new SimulationDsl:
      override def login(): String =
        // TODO - multiple fake users
        requests.post(
          s"${frontend}/login",
          data =
            ujson.Obj("username"->"admin","password" -> "password")
//            requests.RequestBlob.FormEncodedRequestBlob(
//              List(("username", "admin"), ("password", "password"))
//            )
        ).text()
      override def listAuctions(auth: Option[String] = None): Unit =
        auth match
          case Some(token) =>
            val result = requests.get(
              s"${frontend}/",
              auth = requests.RequestAuth.Bearer(token)
            ).text()
            System.err.println(s"Authenticated user saw auctions: $result")
          case None => requests.get(s"${frontend}/").text()
