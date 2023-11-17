package com.google.example

import com.google.example.AuthServer.jwt
import com.google.example.auth.cask.authorized
import com.google.example.o11y.cask.OtelMainRoutes
import pdi.jwt.JwtClaim

/**
 * This server is responsible for understanding user names and passwords and returning
 * JWT tokens with identity/roles.
 */
object AuthServer extends OtelMainRoutes:
  initialize()
  override def port: Int = 8080
  override def host: String = "0.0.0.0"
  private val jwt = auth.Jwt.default

  @cask.postJson("/login_json")
  def loginJson(username: String, password: String) = 
    authorize(username, password)

  @cask.postForm("/login_form")
  def loginForm(username: String, password: String) =
    authorize(username, password)

  // Sends a JWT token and verifies it's still valid and originated from this auth server.
  @authorized // First step of validation of the token.
  @cask.get("/check")
  def check() =
    // TODO - other checks, or "deactivation" of tokens.
    "Ok"


  private def authorize(username: String, password: String) =
    (username, password) match
      case ("admin", "password") =>
        cask.Response(jwt.makeUserToken("admin", Seq("read", "write", "superuser")))
      case ("user", "pw") =>
        cask.Response(jwt.makeUserToken("user", Seq("read")))
      case (user, "password") =>
        // Synthesize user names
        cask.Response(jwt.makeUserToken(user, Seq("read", "write")))
      case _ => cask.Abort(401)