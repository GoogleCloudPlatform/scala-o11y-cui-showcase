package com.google.example

/** This represents actions a fake user can take one the system. */
trait SimulationDsl:
  def login(): String
  def listAuctions(authToken: String): String


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
        ).text()
      override def listAuctions(authToken: String): String =
        val result = requests.get(
              s"${frontend}/",
              auth = requests.RequestAuth.Bearer(authToken)
            ).text()
        result

