package controllers

import models.CreateUserRequest
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Play.materializer
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test.FakeRequest
import utils.Miscs

class UserControllerSpec extends PlaySpec with GuiceOneAppPerSuite {

  val userController: UserController = app.injector.instanceOf[UserController]

  "UserController" should {
    val validId = Miscs.generateUniqueId.substring(0,10)

    "insert a user" in {
      val userRequest = CreateUserRequest(
        "Raj", "1234567890", validId, "mail", "1234", "password"
      )
      val jsonbody = Json.toJson(userRequest)
      val user = userController.createBankUser().apply(FakeRequest(POST, "/users/create").withBody(jsonbody)
        .withHeaders(("Content-Type", "application/json")))
      println(contentAsString(user))
      status(user) mustEqual OK
    }

    "get user by validId" in {
      val user = userController.getBankUserByValidId(validId).apply(FakeRequest(GET, "/users/getBankUserByValidId"))
      status(user) mustEqual OK
      println(contentAsString(user))
    }

    "get user by row" in {
      val user = userController.getBankUsers().apply(FakeRequest(GET, "/users/getBankUserByRow"))
      status(user) mustEqual OK
      println(contentAsString(user))
    }

  }
}
