package controllers

import javax.inject.Inject

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import dao.AccountsDAO
import models.AccountCreateRequest
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.ControllerComponents
import utils.Miscs._

class AccountsController @Inject() (val controllerComponents: ControllerComponents, accountsDAO: AccountsDAO)(
    implicit ec: ExecutionContext
) extends AppBaseController {

  def create(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val createAccountRequest = validateJsonWithCaseClass[AccountCreateRequest](request.body)
    val accountId            = generateUniqueId
    logger.info(s"Creating account with id: $accountId")
    accountsDAO
      .createAccount(accountId, createAccountRequest)
      .map {
        logger.info(s"Account created with id: $accountId")
        rowsCreated =>
          Ok(
            Json.obj(
              "status"      -> 200,
              "description" -> "Account created successfully",
              "accountId"   -> accountId,
              "createdRows" -> rowsCreated
            )
          )
      }
      .recoverWith {
        case ex =>
          logger.error(s"Error creating account with id: $accountId - ${ex.getMessage}")
          Future.successful(InternalServerError(ex.getMessage))
      }
  }

  def getAccountById(accountId: String): Action[AnyContent] = Action.async {
    accountsDAO
      .getAccountById(accountId)
      .map { account =>
        Ok(Json.toJson(account))
      }
      .recoverWith {
        case ex =>
          logger.error(s"Error getting account with id: $accountId - ${ex.getMessage}")
          Future.successful(InternalServerError(ex.getMessage))
      }
  }

}
