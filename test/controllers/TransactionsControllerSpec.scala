package controllers

import models.CreateTransactionRequest
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{POST, contentAsJson, defaultAwaitTimeout, status}
import utils.APICalls._

class TransactionsControllerSpec extends PlaySpec with GuiceOneAppPerSuite {
  "Transactions Controller" should {
    val userController: UserController = app.injector.instanceOf[UserController]
    val accountsController: AccountsController = app.injector.instanceOf[AccountsController]
    val transactionsController: TransactionsController = app.injector.instanceOf[TransactionsController]

    "create and get transactions" in {
      val userIdAndPwd1 = createUser(userController)
      val userId1 = userIdAndPwd1._1
      val pwd1 = userIdAndPwd1._2
      val sessionId1 = createSession(userController, userId1, pwd1)

      val userIdAndPwd2 = createUser(userController)
      val userId2 = userIdAndPwd2._1

      val accountId = createAccount(accountsController, userId1, userId2, sessionId1)

      val transactionBodyJson = Json.toJson(CreateTransactionRequest(accountId, userId1, sessionId1, "deposit", 311.0))
      val transaction = transactionsController.create().apply(FakeRequest(POST, "/transactions/create")
        .withBody(transactionBodyJson)
        .withHeaders(("Content-Type", "application/json")))
      status(transaction) mustEqual 200
      (contentAsJson(transaction) \ "description").as[String] mustEqual "Transaction created successfully"
    }
  }
}
