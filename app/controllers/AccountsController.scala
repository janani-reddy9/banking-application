package controllers

import dao.AccountsDAO
import models.AccountCreateRequest
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import utils.Miscs._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AccountsController @Inject()(val controllerComponents: ControllerComponents, accountsDAO: AccountsDAO)(implicit ec: ExecutionContext) extends BaseController {

  def create(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val createAccountRequest = validateJsonWithCaseClass[AccountCreateRequest](request.body)
    val accountId = generateUniqueId
    accountsDAO.createAccount(accountId, createAccountRequest).map {
      rowsCreated => Ok(
        Json.obj(
          "status" -> 200,
          "description" -> "Account created successfully",
          "accountId" -> accountId,
          "createdRows" -> rowsCreated
        )
      )
    }.recoverWith {
      case ex => Future.successful(InternalServerError(ex.getMessage))
    }
  }

  def getAccountById(accountId: String): Action[AnyContent] = Action.async {
    accountsDAO.getAccountById(accountId).map {
      account => Ok(Json.toJson(account))
    }.recoverWith {
      case ex => Future.successful(InternalServerError(ex.getMessage))
    }
  }

}
