package controllers

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.play.PlaySpec
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.contentAsString
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.Helpers.status
import play.api.test.Helpers.POST
import utils.APICalls._

class AccountsControllerSpec extends PlaySpec with GuiceOneAppPerSuite {

  val userController: UserController         = app.injector.instanceOf[UserController]
  val accountsController: AccountsController = app.injector.instanceOf[AccountsController]

  "AccountsController" should {
    "create account and get - positive test cases" in {
      val userIdAndPwd1 = createUser(userController)
      val userId1       = userIdAndPwd1._1
      val pwd1          = userIdAndPwd1._2
      val sessionId1    = createSession(userController, userId1, pwd1)

      val userIdAndPwd2 = createUser(userController)
      val userId2       = userIdAndPwd2._1

      val accountId = createAccount(accountsController, userId1, List(userId2), sessionId1, "2")
      getAccount(accountsController, accountId).nonEmpty mustEqual true
    }

    "throw exception" in {
      val userIdAndPwd1 = createUser(userController)
      val userId1       = userIdAndPwd1._1
      val pwd1          = userIdAndPwd1._2
      val sessionId1    = createSession(userController, userId1, pwd1)

      val userIdAndPwd2 = createUser(userController)
      val userId2       = userIdAndPwd2._1

      val createAccountRequest     = models.AccountCreateRequest(userId1, sessionId1, List(userId2), "1", 15000)
      val createAccountRequestJson = Json.toJson(createAccountRequest)
      val createAccountRes = accountsController
        .create()
        .apply(
          FakeRequest(POST, "/accounts/createAccount")
            .withBody(createAccountRequestJson)
            .withHeaders(("Content-Type", "application/json"))
        )
      status(createAccountRes) mustEqual INTERNAL_SERVER_ERROR
      contentAsString(createAccountRes) mustEqual "Only one user is allowed"
    }
  }
}
