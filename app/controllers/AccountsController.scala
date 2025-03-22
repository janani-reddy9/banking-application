package controllers

import dao.AccountsDAO
import models.AccountCreateRequest
import play.api.{Logger, Logging}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import utils.Miscs._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AccountsController @Inject()(val controllerComponents: ControllerComponents, accountsDAO: AccountsDAO)(implicit ec: ExecutionContext)
  extends Logging with BaseController {

  def create(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val createAccountRequest = validateJsonWithCaseClass[AccountCreateRequest](request.body)
    val accountId = generateUniqueId
    logger.info(s"Creating account with id: $accountId")
    accountsDAO.createAccount(accountId, createAccountRequest).map {
      logger.info(s"Account created with id: $accountId")
      rowsCreated => Ok(
        Json.obj(
          "status" -> 200,
          "description" -> "Account created successfully",
          "accountId" -> accountId,
          "createdRows" -> rowsCreated
        )
      )
    }.recoverWith {
      case ex =>
        logger.error(s"Error creating account with id: $accountId - ${ex.getMessage}")
        Future.successful(InternalServerError(ex.getMessage))
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
