package controllers

import models.CreateUserRequest
import models.FieldDetails
import models.UpdateUserRequest
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.Play.materializer
import utils.Miscs

class UserControllerSpec extends PlaySpec with GuiceOneAppPerSuite {

  val userController: UserController = app.injector.instanceOf[UserController]

  "UserController" should {
    val validId = Miscs.generateUniqueId.substring(0, 10)

    "valid positive cases of Users APIs" in {
      val createUserRequest = CreateUserRequest(
        "Raj",
        "1234567890",
        validId,
        "mail",
        "1234",
        "password"
      )
      val jsonbody = Json.toJson(createUserRequest)
      val user = userController
        .createBankUser()
        .apply(
          FakeRequest(POST, "/users/create")
            .withBody(jsonbody)
            .withHeaders(("Content-Type", "application/json"))
        )
      status(user) mustEqual OK
      (contentAsJson(user) \ "description").as[String] mustEqual "User created successfully"

      val getUserIdRes =
        userController.getBankUserByValidId(validId).apply(FakeRequest(GET, "/users/getBankUserByValidId"))
      status(getUserIdRes) mustEqual OK
      (contentAsJson(getUserIdRes) \ "description").as[String] mustEqual "User retrieved successfully"
      val userId = (contentAsJson(getUserIdRes) \ "id").as[String]

      val sessionIdRes = userController
        .login(userId, createUserRequest.password)
        .apply(FakeRequest(GET, s"/users/login/$userId/${createUserRequest.password}"))
      status(sessionIdRes) mustEqual OK
      val sessionId = (contentAsJson(sessionIdRes) \ "sessionId").as[String]

      val updateUserRequest     = UpdateUserRequest(userId, sessionId, Seq(FieldDetails("username", "raju")))
      val updateUserRequestJson = Json.toJson(updateUserRequest)
      val updatedUserRes = userController
        .updateUser()
        .apply(
          FakeRequest(PUT, "/users/update")
            .withBody(updateUserRequestJson)
            .withHeaders(("Content-Type", "application/json"))
        )
      status(updatedUserRes) mustEqual OK
      (contentAsJson(updatedUserRes) \ "description").as[String] mustEqual "User updated successfully"
    }
  }

}
