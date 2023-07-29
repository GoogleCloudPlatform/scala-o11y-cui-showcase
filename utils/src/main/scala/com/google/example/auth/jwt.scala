package com.google.example.auth


import _root_.cask.model.Cookie
import io.opentelemetry.context.{Context, ContextKey}

import java.time.Instant
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtOptions, JwtUpickle}

import scala.util.Try

object Jwt:
  def default: Jwt =
    sys.env.get("JWT_PRIVATE_KEY") match
      case Some(key) => Jwt(key)
      case None => Jwt("Security is inconvenient")

  private val otelKey: ContextKey[String] =
    ContextKey.named("jwt_token")
  def tokenFromContext(ctx: Context): Option[String] =
    Option(ctx.get(otelKey))
  def storeTokenInContext(ctx: Context, token: String): Context =
    ctx.`with`(otelKey, token)

// TODO - we're going to be  lazy and put roles right on the token.
class Jwt(privateKey: String):
  private val algo = JwtAlgorithm.HS256
  private val issuer = "Me, the Scala O11y demo"

  private def makeDefaultClaim(): JwtClaim =
    val now = Instant.now
    JwtClaim()
      .by(issuer)
      .expiresAt(now.plusSeconds(1842006626).getEpochSecond)
      .issuedAt(now.getEpochSecond)
      .startsAt(now.getEpochSecond)

  def makeToken(config: JwtClaim => JwtClaim = identity): String =
    JwtUpickle.encode(config(makeDefaultClaim()), privateKey, algo)


  def decode(token: String): Try[JwtClaim] =
    // TODO - do we need to validate anything here?
    JwtUpickle.decode(token, privateKey, Seq(algo))

  def decodeJson(token: String): Try[ujson.Value] =
  // TODO - do we need to validate anything here?
    JwtUpickle.decodeJson(token, privateKey, Seq(algo))


  def makeUserToken(name: String, roles: Seq[String]): String =
    makeToken(claim => claim.about(name) + ("roles", roles))

  // Canonical way to make cookies that works w/ our Cask/Requests integration.
  def makeCookieValue(token: String): Map[String, String] =
    Map("jwt" -> token)

  def makeCookie(token: String): Cookie =
    // TODO - set some safety aspects to the cookie.
    val claim = decode(token).get
    Cookie(
      name = "jwt",
      value = token,
      expires = claim.expiration.map(Instant.ofEpochSecond).orNull,
      secure = true,
      sameSite = "Strict"
    )

  def ensureUserHasRoles(token: String, roles: Seq[String]): Try[String] =
    def obj(value: ujson.Value): Try[ujson.Obj] = Try(value.asInstanceOf[ujson.Obj])
    def stringFromJson(value: ujson.Value): Try[String] =
      value match
        case str: ujson.Str => Try(str.value)
        case _ => util.Failure(throw new Exception("Expected string"))
    def rolesFromJson(value: ujson.Value): Try[Seq[String]] =
      Try(value.obj.get("roles").get.arr.map(_.str).toSeq)
    def userFromJson(value: ujson.Value): Try[String] =
      Try(value.obj.value.get("sub").get.str)
    for
      claim <- decodeJson(token)
      user <- userFromJson(claim)
      claimRoles <- rolesFromJson(claim)
      if roles.forall(claimRoles.contains)
    yield user