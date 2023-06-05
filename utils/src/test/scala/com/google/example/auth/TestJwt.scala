package com.google.example.auth

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class TestJwt:
  val jwt = Jwt.default
  @Test
  def testEncodeAndDecode(): Unit =
    val token = jwt.makeToken(_.about("Josh"))
    val result = jwt.decode(token).get
    assertEquals("Josh", result.subject.get)


  @Test
  def testCheckRoles(): Unit =
    val token = jwt.makeUserToken("Josh", Seq("awesome", "admin"))
    // Ensure we get user name back when ensuring roles.
    assertEquals("Josh", jwt.ensureUserHasRoles(token, Seq("awesome")).get)
    // Ensure all roles are found.
    assertTrue(jwt.ensureUserHasRoles(token, Seq("awesome", "admin")).isSuccess)
    // Ensure bogus roles cause failure.
    assertTrue(jwt.ensureUserHasRoles(token, Seq("awesome", "superhero")).isFailure)
    // Ensure only bogus roles cause failure.
    assertTrue(jwt.ensureUserHasRoles(token, Seq("superhero")).isFailure)
    // Ensure no roles is success
    assertFalse(jwt.ensureUserHasRoles(token, Seq()).isFailure)
