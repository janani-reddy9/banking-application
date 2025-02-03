package utils

import controllers.{AccountsController, UserController}
import models.CreateUserRequest
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._

object APICalls {

  def createUser(userController: UserController): (String, String) = {
    val validId = Miscs.generateUniqueId.substring(0, 10)
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

  def createSession(userController: UserController,userId: String, password: String): String = {
    val sessionIdRes = userController.login(userId, password).apply(FakeRequest(GET, s"/users/login/$userId/$password}"))
    status(sessionIdRes) mustEqual OK
    (contentAsJson(sessionIdRes) \ "sessionId").as[String]
  }

  def createAccount(accountsController: AccountsController, userId1: String, userId2: String, sessionId: String): String = {
    val createAccountRequest = models.AccountCreateRequest(userId1, sessionId, List(userId2), "2", 15000)
    val createAccountRequestJson = Json.toJson(createAccountRequest)
    val createAccountRes = accountsController.create().apply(FakeRequest(POST, "/accounts/createAccount").withBody(createAccountRequestJson)
      .withHeaders(("Content-Type", "application/json")))
    status(createAccountRes) mustEqual OK
    (contentAsJson(createAccountRes) \ "accountId").as[String]
  }

  def getAccount(accountsController: AccountsController, accountId: String): String = {
    val getAccountRes = accountsController.getAccountById(accountId).apply(FakeRequest(GET, s"/accounts/getAccount/$accountId"))
    println(contentAsString(getAccountRes))
    status(getAccountRes) mustEqual OK
    (contentAsJson(getAccountRes) \ "accountId").as[String]
  }

}
