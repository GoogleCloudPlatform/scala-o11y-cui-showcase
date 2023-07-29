package com.google.example.auth
package cask

import _root_.cask.{Abort, Redirect, RawDecorator, Request}
import _root_.cask.router.{Result}
import _root_.cask.model.Response.{Raw}
import _root_.cask.model.Response
import io.opentelemetry.context.Context
import scala.util.matching.Regex

/** Ensures a request includes a JWT authorized token, optionally with given roles. */
class authorized(val roles: Seq[String] = Nil, val redirect: Option[String] = None) extends RawDecorator:
  private val jwt = Jwt.default
  private val redirectResult: Option[Result[Raw]] =
    redirect.map(uri => Result.Success(Redirect(uri)))

  private val BearerRegex = "Bearer \"([^\"]+)\"".r

  override def wrapFunction(ctx: Request, delegate: Delegate): Result[Raw] =
    tokenFromRequest(ctx) match
      case Some(token) =>
        try
          val user = jwt.ensureUserHasRoles(token, roles).get
          // If we get a valid token, we'll put it into Context.
          scala.util.Using.Manager { use =>
            use(Jwt.storeTokenInContext(Context.current(), token).makeCurrent())
            delegate(Map("user" -> user))
          }.get
        catch
          case ex: Exception =>
            System.err.println(s"Failed to verify user for roles: $roles")
            ex.printStackTrace()
            redirectResult getOrElse Result.Success(Abort(403))
      case _ =>
        redirectResult getOrElse Result.Success(Abort(401))

  // Determines if there *is* a user token in a request
  private def tokenFromRequest(ctx: Request): Option[String] =
    def tokenFromCookie: Option[String] =
      ctx.cookies.get("jwt").map(_.value)
    def tokenFromAuthorizationHeader: Option[String] =
      Option(ctx.exchange.getRequestHeaders.getFirst("Authorization")) match
        case Some(BearerRegex(value)) => Some(value)
        case Some(value) if value.startsWith("Bearer ") => Some(value.drop(7))
        case _ => None
    def tokenFromAccessTokenHeader: Option[String] =
      Option(ctx.exchange.getRequestHeaders.getFirst("x-access-token"))
    // We pull tokens in this prioritizatio order:
    // 1. `Authorization` Header
    // 2. `jwt` Cookie
    // 3. `x-access-token` header
    tokenFromAuthorizationHeader orElse tokenFromCookie orElse tokenFromAccessTokenHeader
