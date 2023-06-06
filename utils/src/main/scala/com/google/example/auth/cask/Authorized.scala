package com.google.example.auth
package cask

import _root_.cask.{Abort, Redirect, RawDecorator, Request}
import _root_.cask.router.{Result}
import _root_.cask.model.Response.{Raw}
import _root_.cask.model.Response

/** Ensures a request includes a JWT authorized token, optionally with given roles. */
class authorized(val roles: Seq[String] = Nil, val redirect: Option[String] = None) extends RawDecorator:
  private val jwt = Jwt.default
  private val redirectResult: Option[Result[Raw]] =
    redirect.map(uri => Result.Success(Redirect(uri)))

  override def wrapFunction(ctx: Request, delegate: Delegate): Result[Raw] =
    tokenFromRequest(ctx) match
      case Some(token) =>
        try
          val user = jwt.ensureUserHasRoles(token, roles).get
          // If we get a valid token, we should put it in Context for propagation.
          delegate(Map("user" -> user))
        catch
          case ex: Exception =>
            redirectResult getOrElse Result.Success(Abort(403))
      case _ =>
        println(s"About to abort $ctx, with redirect: $redirectResult")
        redirectResult getOrElse Result.Success(Abort(401))

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
