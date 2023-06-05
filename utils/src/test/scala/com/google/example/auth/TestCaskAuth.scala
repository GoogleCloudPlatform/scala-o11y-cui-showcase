package com.google.example.auth

import io.undertow.Undertow
import _root_.cask.{MainRoutes, get}
import com.google.example.auth.cask.authorized
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import requests.RequestAuth.Bearer
import requests.RequestFailedException

class TestCaskAuth:
  val jwt = Jwt.default
  @Test
  def testAuthorizedEndpoint(): Unit =
    withServer(MyTestAuthApp) { url =>
      // Check Authorization/Bearer header.
      assertEquals("Hello, World", requests.get(url,
        auth=Bearer(jwt.makeUserToken("World", Seq("read")))).text())
      // Check x-access-token header.
      assertEquals("Hello, Josh", requests.get(url,
        headers = Seq("x-access-token"-> jwt.makeUserToken("Josh", Seq("read")))).text())
      // Check we get unauthorized
      assertThrows(classOf[RequestFailedException], () => requests.get(url))
      assertThrows(classOf[RequestFailedException], () => requests.get(url, auth=Bearer(jwt.makeUserToken("bad", Seq()))))
    }



  // Starts a CASK server and returns the URL for it.
  def withServer[T](example: _root_.cask.main.Main)(f: String => T): T =
    val server = Undertow.builder
      .addHttpListener(8082, "localhost")
      .setHandler(example.defaultHandler)
      .build
    server.start()
    val res =
      try f("http://localhost:8082")
      finally server.stop()
    res

object MyTestAuthApp extends _root_.cask.MainRoutes:
  initialize()
  @authorized(Seq("read"))
  @get("/")
  def index()(user: String) = s"Hello, ${user}"