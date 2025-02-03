package controllers

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import utils.APICalls._

class AccountsControllerSpec extends PlaySpec with GuiceOneAppPerSuite {

  val userController: UserController = app.injector.instanceOf[UserController]
  val accountsController: AccountsController = app.injector.instanceOf[AccountsController]

  "AccountsController" should {
    "create account and get - positive test cases" in {
      val userIdAndPwd1 = createUser(userController)
      val userId1 = userIdAndPwd1._1
      val pwd1 = userIdAndPwd1._2
      val sessionId1 = createSession(userController, userId1, pwd1)

      val userIdAndPwd2 = createUser(userController)
      val userId2 = userIdAndPwd2._1

      val accountId = createAccount(accountsController, userId1, userId2, sessionId1)
      getAccount(accountsController, accountId).nonEmpty mustEqual true
    }
  }
}
