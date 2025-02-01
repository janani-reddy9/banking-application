package controllers

import dao.AccountsDAO
import models.AccountCreateRequest
import play.api.libs.json.Json
import play.api.mvc.{BaseController, ControllerComponents}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AccountsController @Inject()(val controllerComponents: ControllerComponents, accountsDAO: AccountsDAO)(implicit ec: ExecutionContext) extends BaseController {

  def create() = Action.async(parse.json) { implicit request =>
    val createAccountRequest = Json.parse(request.body.toString()).as[AccountCreateRequest]
    accountsDAO.createAccount(createAccountRequest.userId, createAccountRequest.sessionId, createAccountRequest.userIds, createAccountRequest.accountTypeId, createAccountRequest.balance).map {
      case Left(e) => BadRequest(e.getMessage)
      case Right(_) => Ok(
        Json.obj(
          "status" -> 200,
          "description" -> "Account created successfully"
        )
      )
    }.recoverWith {
      case ex => Future.successful(InternalServerError(ex.getMessage))
    }
  }

}
