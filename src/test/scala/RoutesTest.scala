/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import scala.concurrent.Future
import akka.actor.ActorSystem
import akka.http.scaladsl.testkit.RouteTestTimeout
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.model.{
  ContentTypes,
  FormData,
  HttpMethods,
  HttpRequest,
  StatusCodes
}
import akka.http.scaladsl.model.headers.Cookie
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.Matchers
import org.scalatest.funsuite.AnyFunSuite
import org.scalamock.scalatest.MockFactory

import concurrent.duration._
import poca.{
  Basket,
  MailException,
  MyDatabase,
  Order,
  Orders,
  Product,
  Products,
  Routes,
  User,
  UserAlreadyExistsException,
  Users,
  Requested,
}
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.StatusCode

import java.util.Date
import java.sql.Timestamp

class RoutesTest
    extends AnyFunSuite
    with Matchers
    with MockFactory
    with ScalatestRouteTest {

  // the Akka HTTP route testkit does not yet support a typed actor system (https://github.com/akka/akka-http/issues/2036)
  // so we have to adapt for now
  lazy val testKit = ActorTestKit()
  implicit def default(implicit system: ActorSystem) =
    RouteTestTimeout(10.second)
  implicit def typedSystem = testKit.system
  override def createActorSystem(): akka.actor.ActorSystem =
    testKit.system.classicSystem

  test("Route GET /hello should say hello") {
    var mockUsers       = mock[Users]
    var mockProducts    = mock[Products]
    var mockOrders      = mock[Orders]
    val routesUnderTest = new Routes(mockUsers, mockProducts, mockOrders).routes

    val request = HttpRequest(uri = "/hello")
    request ~> routesUnderTest ~> check {
      status should ===(StatusCodes.OK)

      contentType should ===(ContentTypes.`text/html(UTF-8)`)

      entityAs[String] should ===("<h1>Say hello to akka-http</h1>")
    }
  }

  test("Route GET /signup should returns the signup page") {
    var mockUsers       = mock[Users]
    var mockProducts    = mock[Products]
    var mockOrders      = mock[Orders]
    val routesUnderTest = new Routes(mockUsers, mockProducts, mockOrders).routes

    val request = HttpRequest(uri = "/signup")
    request ~> routesUnderTest ~> check {
      status should ===(StatusCodes.OK)

      contentType should ===(ContentTypes.`text/html(UTF-8)`)
    }
  }

  test("Route POST /api/register should create a new user") {
    var mockUsers    = mock[Users]
    var mockProducts = mock[Products]
    var mockOrders   = mock[Orders]

    (mockUsers.createUser _)
      .expects("toto", "password", "test@test.fr", false)
      .returning(Future(()))
      .once()

    val routesUnderTest = new Routes(mockUsers, mockProducts, mockOrders).routes

    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = "/api/register",
      entity = FormData(
        ("username", "toto"),
        ("password", "password"),
        ("email", "test@test.fr")
      ).toEntity
    )
    request ~> routesUnderTest ~> check {
      status should ===(StatusCodes.OK)

      contentType should ===(ContentTypes.`text/plain(UTF-8)`)

      entityAs[String] should ===(
        "Welcome 'toto'! You've just been registered to our great marketplace."
      )
    }
  }

  test(
    "Route POST /api/register should warn the user when username is already taken"
  ) {
    var mockUsers    = mock[Users]
    var mockProducts = mock[Products]
    var mockOrders   = mock[Orders]

    (mockUsers.createUser _)
      .expects("toto", "password", "test@test.fr", false)
      .returns(Future({
        throw new UserAlreadyExistsException("")
      }))
      .once()

    val routesUnderTest = new Routes(mockUsers, mockProducts, mockOrders).routes

    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = "/api/register",
      entity = FormData(
        ("username", "toto"),
        ("password", "password"),
        ("email", "test@test.fr")
      ).toEntity
    )
    request ~> routesUnderTest ~> check {
      status should ===(StatusCodes.OK)

      contentType should ===(ContentTypes.`text/plain(UTF-8)`)

      entityAs[String] should ===(
        "The username 'toto' is already taken. Please choose another username."
      )
    }
  }

  test(
    "Route POST /api/register should raise exception if mail format isn't good"
  ) {
    var mockUsers    = mock[Users]
    var mockProducts = mock[Products]
    var mockOrders   = mock[Orders]

    (mockUsers.createUser _)
      .expects("toto", "password", "test.fr", false)
      .returns(Future({
        throw new MailException()
      }))
      .once()

    val routesUnderTest = new Routes(mockUsers, mockProducts, mockOrders).routes

    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = "/api/register",
      entity = FormData(
        ("username", "toto"),
        ("password", "password"),
        ("email", "test.fr")
      ).toEntity
    )
    request ~> routesUnderTest ~> check {
      status should ===(StatusCodes.BadRequest)
      contentType should ===(ContentTypes.`text/plain(UTF-8)`)
      entityAs[String] should ===(
        s"The mail 'test.fr' is not well format. Check that you write it correctly or choose another one."
      )
    }
  }

  test(
    "Route POST /api/register should return empty fields if fields are missing"
  ) {
    var mockUsers    = mock[Users]
    var mockProducts = mock[Products]
    var mockOrders   = mock[Orders]

    val routesUnderTest = new Routes(mockUsers, mockProducts, mockOrders).routes

    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = "/api/register",
      entity = FormData(
        ("username", "toto"),
        ("email", "test.fr")
      ).toEntity
    )
    request ~> routesUnderTest ~> check {
      status should ===(StatusCodes.BadRequest)
      contentType should ===(ContentTypes.`text/plain(UTF-8)`)
      entityAs[String] should ===(
        "Field 'username' or 'password' not found."
      )
    }
  }



//test important
  

  test("Route GET /profile?userId=hello shouldn't display the user hello") {
    val mockUsers       = mock[Users]
    val mockProducts    = mock[Products]
    var mockOrders      = mock[Orders]
    val routesUnderTest = new Routes(mockUsers, mockProducts, mockOrders).routes

    // A profile that does not exist
    Get("/profile?userId=hello") ~> routesUnderTest ~> check {
      status should ===(StatusCodes.SeeOther)
    }

  }

  test("Route GET /profile?userId=0 should display user 0") {
    var mockUsers    = mock[Users]
    var mockProducts = mock[Products]
    var mockOrders   = mock[Orders]
    val user = User(
      userId = "0",
      username = "test",
      password = "test",
      email = "test@test.fr",
      isSeller = false
    )
    val routesUnderTest = new Routes(mockUsers, mockProducts, mockOrders).routes

    Get("/profile?userId=0") ~> routesUnderTest ~> check {
      status should ===(StatusCodes.SeeOther)
      contentType should ===(ContentTypes.`text/html(UTF-8)`)
    }
  }

  test("Route GET / should redirect to /signin ") {
    var mockUsers       = mock[Users]
    var mockProducts    = mock[Products]
    var mockOrders      = mock[Orders]
    val routesUnderTest = new Routes(mockUsers, mockProducts, mockOrders).routes

    Get("/") ~> routesUnderTest ~> check {
      status shouldEqual StatusCodes.PermanentRedirect
      responseAs[String] shouldEqual """The request, and all future requests should be repeated using <a href="/signin">this URI</a>."""
    }
  }

  test("Route GET /signin should return signin page if not signed in") {
    var mockUsers       = mock[Users]
    var mockProducts    = mock[Products]
    var mockOrders      = mock[Orders]
    val routesUnderTest = new Routes(mockUsers, mockProducts, mockOrders).routes

    Get("/signin") ~> routesUnderTest ~> check {
      status shouldEqual StatusCodes.OK
      contentType should ===(ContentTypes.`text/html(UTF-8)`)
    }
  }

  def checkSetCookies(cookies: Array[String], true_status: StatusCode) = {
    check {
      status should ===(true_status)
      contentType should ===(ContentTypes.`text/html(UTF-8)`)

      for (i <- 0 to 2) {
        headers(i).name should ===("Set-Cookie")
        cookies(i) = headers(i).value().split(";")(0)
      }
    }
  }

  def requestCookies(request: HttpRequest, cookies: Array[String]) = {
    var r = request
    for (c <- cookies) {
      var s = c.split("=");
      r = r ~> Cookie(s(0) -> s(1))
    }
    r
  }

  def createSession(
      user: User,
      mockUsers: Users,
      mockProducts: Products,
      routesUnderTest: akka.http.scaladsl.server.Route,
      cookies: Array[String] = new Array[String](3)
  ) = {
    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = "/api/do_login",
      entity = FormData(
        ("username", user.username),
        ("password", user.password)
      ).toEntity
    )

    (mockUsers.getUserByUsername _)
      .expects(user.username)
      .returns(Future(Some(user)))
      .anyNumberOfTimes()
    (mockUsers.checkUserPassword _)
      .expects(user, user.username)
      .returns(true)
      .once()

    val res = {
      if (cookies(0) == null) {
        request
      } else {
        requestCookies(request, cookies)
      }
    } ~> routesUnderTest

    res ~> checkSetCookies(cookies, StatusCodes.SeeOther)
    res ~> check {
      headers(3).name should ===("Location")
      headers(3).value should ===(s"/profile?userId=${user.userId}")
    }

    cookies
  }

  def idFromCookie(cookie: String): String = {
    cookie.split("~")(1).split("&")(0)
  }

  def idFromCookie(cookies: Array[String]): String = {
    var out = ""
    for (c <- cookies) {
      if (c.split("=")(0) == "_sessiondata") {
        out = idFromCookie(c)
      }
    }
    out
  }

  def basketFromCookie(cookie: String): Basket = {
    val s = cookie.split("~")
    if (s.length < 3) {
      new Basket()
    } else {
      new Basket(s(2).replaceAll("%3A", ":"))
    }
  }

  def basketFromCookie(cookies: Array[String]): Basket = {
    var out = new Basket
    for (c <- cookies) {
      if (c.split("=")(0) == "_sessiondata") {
        out = basketFromCookie(c)
      }
    }
    out
  }

  def createEmptySession(
      routesUnderTest: akka.http.scaladsl.server.Route,
      cookies: Array[String] = new Array[String](3)
  ): Array[String] = {
    val res = Post(
      "/api/add_basket",
      FormData(
        ("id", "0")
      )
    ) ~> routesUnderTest
    res ~> checkSetCookies(cookies, StatusCodes.SeeOther)
    res ~> check {
      headers(3).name should ===("Location")
      headers(3).value should ===("/basket")

      basketFromCookie(cookies).size should ===(1)
      idFromCookie(cookies) should ===("")
    }

    cookies
  }

  // def testCookies(body)

  test("Route POST /api/do_login should return cookies if user exists") {
    var mockUsers       = mock[Users]
    var mockProducts    = mock[Products]
    var mockOrders      = mock[Orders]
    val routesUnderTest = new Routes(mockUsers, mockProducts, mockOrders).routes

    val user = User(
      userId = "0",
      username = "test",
      password = "test",
      email = "test@test.fr",
      isSeller = false
    )

    createSession(user, mockUsers, mockProducts, routesUnderTest)
  }

  test("Route POST /api/do_login should not login if user doesn't exist") {
    var mockUsers    = mock[Users]
    var mockProducts = mock[Products]
    var mockOrders   = mock[Orders]

    val user = User(
      userId = "0",
      username = "test",
      password = "test",
      email = "test@test.fr",
      isSeller = false
    )
    val routesUnderTest = new Routes(mockUsers, mockProducts, mockOrders).routes

    (mockUsers.getUserByUsername _)
      .expects("test")
      .returns(Future(Some(user)))
      .anyNumberOfTimes()
    (mockUsers.checkUserPassword _).expects(user, "test").returns(false).once()

    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = "/api/do_login",
      entity = FormData(("username", "test"), ("password", "test")).toEntity
    )

    request ~> routesUnderTest ~> check {
      status should ===(StatusCode.int2StatusCode(403))
    }
  }

  test(
    "Route POST /api/add_basket should add product to current basket whether a session is present or not"
  ) {
    var mockUsers       = mock[Users]
    var mockProducts    = mock[Products]
    var mockOrders      = mock[Orders]
    val routesUnderTest = new Routes(mockUsers, mockProducts, mockOrders).routes

    val user = User(
      userId = "0",
      username = "test",
      password = "test",
      email = "test@test.fr",
      isSeller = false
    )

    var cookies = createSession(user, mockUsers, mockProducts, routesUnderTest)

    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = "/api/add_basket",
      entity = FormData(
        ("id", "0")
      ).toEntity
    )

    val res = requestCookies(request, cookies) ~> routesUnderTest

    res ~> checkSetCookies(cookies, StatusCodes.SeeOther)
    res ~> check {
      headers(3).name should ===("Location")
      headers(3).value should ===("/basket")

      basketFromCookie(cookies).size should ===(1)
      idFromCookie(cookies) should ===("0")
    }

    val res2 = request ~> routesUnderTest
    res2 ~> checkSetCookies(cookies, StatusCodes.SeeOther)
    res2 ~> check {
      headers(3).name should ===("Location")
      headers(3).value should ===("/basket")

      basketFromCookie(cookies).size should ===(1)
      idFromCookie(cookies) should ===("")
    }

    Post("/api/add_basket") ~> routesUnderTest ~> check {
      status should ===(StatusCodes.NotFound)
    }
  }

  test(
    "Route POST /api/do_login should keep basket of empty session"
  ) {
    var mockUsers       = mock[Users]
    var mockProducts    = mock[Products]
    var mockOrders      = mock[Orders]
    val routesUnderTest = new Routes(mockUsers, mockProducts, mockOrders).routes

    val user = User(
      userId = "0",
      username = "test",
      password = "test",
      email = "test@test.fr",
      isSeller = false
    )

    var cookies = createEmptySession(routesUnderTest)

    cookies =
      createSession(user, mockUsers, mockProducts, routesUnderTest, cookies)
    basketFromCookie(cookies).size should ===(1)
  }

  test(
    "Route POST /api/remove_basket tests"
  ) {
    var mockUsers       = mock[Users]
    var mockProducts    = mock[Products]
    var mockOrders      = mock[Orders]
    val routesUnderTest = new Routes(mockUsers, mockProducts, mockOrders).routes

    var cookies = createEmptySession(routesUnderTest)

    val request2 = HttpRequest(
      method = HttpMethods.POST,
      uri = "/api/remove_basket",
      entity = FormData(
        ("id", "0")
      ).toEntity
    )

    val res2 = requestCookies(request2, cookies) ~> routesUnderTest
    res2 ~> checkSetCookies(cookies, StatusCodes.SeeOther)
    res2 ~> check {
      headers(3).name should ===("Location")
      headers(3).value should ===("/basket")

      basketFromCookie(cookies).size should ===(0)
      idFromCookie(cookies) should ===("")
    }

    request2 ~> routesUnderTest ~> check {
      status should ===(StatusCodes.OK)
    }

    val request3 = HttpRequest(
      method = HttpMethods.POST,
      uri = "/api/remove_basket"
    )

    request3 ~> routesUnderTest ~> check {
      status should ===(StatusCodes.NotFound)
    }
  }

  test("Route GET /api/current_login test") {
    var mockUsers       = mock[Users]
    var mockProducts    = mock[Products]
    var mockOrders      = mock[Orders]
    val routesUnderTest = new Routes(mockUsers, mockProducts, mockOrders).routes

    val user = User(
      userId = "0",
      username = "test",
      password = "test",
      email = "test@test.fr",
      isSeller = false
    )

    var cookies = createSession(user, mockUsers, mockProducts, routesUnderTest)

    requestCookies(
      Get("/api/current_login"),
      cookies
    ) ~> routesUnderTest ~> check {
      status should ===(StatusCodes.OK)
      responseAs[String] shouldEqual (s"${user.userId}")
    }
  }

  test("Route GET /password test") {
    var mockUsers       = mock[Users]
    var mockProducts    = mock[Products]
    var mockOrders      = mock[Orders]
    val routesUnderTest = new Routes(mockUsers, mockProducts, mockOrders).routes

    Get("/password") ~> routesUnderTest ~> check(
      status should ===(StatusCodes.OK)
    )
  }



  





  

 



 

 

  

 
}
