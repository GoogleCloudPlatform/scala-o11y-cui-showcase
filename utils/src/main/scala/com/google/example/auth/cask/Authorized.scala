package com.google.example.auth
package cask

import _root_.cask.{Abort, RawDecorator, Request}
import _root_.cask.router.{Result}
import _root_.cask.model.Response.{Raw}

/** Ensures a request includes a JWT authorized token, optionally with given roles. */
class authorized(val roles: Seq[String] = Nil) extends RawDecorator:
  val jwt = Jwt.default

  override def wrapFunction(ctx: Request, delegate: Delegate): Result[Raw] =
    tokenFromRequest(ctx) match
      case Some(token) =>
        try
          val user = jwt.ensureUserHasRoles(token, roles).get
          // If we get a valid token, we should put it in Context for propagation.
          delegate(Map("user" -> user))
        catch
          case ex: Exception =>
            // TODO - log this?
            Result.Success(Abort(403))
      case _ => Result.Success(Abort(401))

  // Determines if there *is* a user token in a request
  private def tokenFromRequest(ctx: Request): Option[String] =
    def tokenFromAuthorizationHeader: Option[String] =
      Option(ctx.exchange.getRequestHeaders.getFirst("Authorization")) match
        case Some(value) if value.startsWith("Bearer ") => Some(value.drop(7))
        case _ => None
    def tokenFromAccessTokenHeader: Option[String] =
      Option(ctx.exchange.getRequestHeaders.getFirst("x-access-token"))
    // TODO - token from Cookie?
    tokenFromAuthorizationHeader orElse tokenFromAccessTokenHeader
