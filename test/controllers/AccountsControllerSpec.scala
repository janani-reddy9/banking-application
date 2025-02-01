package controllers

import models.CreateUserRequest
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test.FakeRequest
import utils.Miscs


class AccountsControllerSpec extends PlaySpec with GuiceOneAppPerSuite {

  val userController: UserController = app.injector.instanceOf[UserController]
  val accountsController: AccountsController = app.injector.instanceOf[AccountsController]

  "AccountsController" should {

    "create account and get - positive test cases" in {

      val userIdAndPwd1 = createUser()
      val userId1 = userIdAndPwd1._1
      val pwd1 = userIdAndPwd1._2
      val sessionId1 = createSession(userId1, pwd1)

      val userIdAndPwd2 = createUser()
      val userId2 = userIdAndPwd2._1

      val createAccountRequest = models.AccountCreateRequest(userId1, sessionId1, List(userId2), 2, 15000)
      val createAccountRequestJson = Json.toJson(createAccountRequest)
      val createAccountRes = accountsController.create().apply(FakeRequest(POST, "/accounts/createAccount").withBody(createAccountRequestJson)
        .withHeaders(("Content-Type", "application/json")))
      println(contentAsString(createAccountRes))
      status(createAccountRes) mustEqual OK
    }

    def createUser(): (String, String) = {
      val validId = Miscs.generateUniqueId.substring(0,10)
      val createUserRequest = CreateUserRequest(
        "Raj", "1234567890", validId, "mail", "1234", "password"
      )
      val jsonbody = Json.toJson(createUserRequest)
      val user = userController.createBankUser().apply(FakeRequest(POST, "/users/create").withBody(jsonbody)
        .withHeaders(("Content-Type", "application/json")))
      println(contentAsString(user))
      status(user) mustEqual OK
      (contentAsJson(user) \ "description").as[String] mustEqual "User created successfully"

      val getUserIdRes = userController.getBankUserByValidId(validId).apply(FakeRequest(GET, "/users/getBankUserByValidId"))
      status(getUserIdRes) mustEqual OK
      (contentAsJson(getUserIdRes) \ "description").as[String] mustEqual "User retrieved successfully"
      val userId = (contentAsJson(getUserIdRes) \ "id").as[String]
      (userId, createUserRequest.password)
    }

    def createSession(userId: String, password: String): String = {
      val sessionIdRes = userController.login(userId, password).apply(FakeRequest(GET, s"/users/login/$userId/$password}"))
      status(sessionIdRes) mustEqual OK
      (contentAsJson(sessionIdRes) \ "sessionId").as[String]
    }
  }

}
