package controllers

import javax.inject.Inject

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import dao.TransactionsDAO
import models.CreateTransactionRequest
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.BaseController
import play.api.mvc.ControllerComponents
import utils.Miscs._

class TransactionsController @Inject() (
    val controllerComponents: ControllerComponents,
    transactionsDAO: TransactionsDAO
)(implicit ec: ExecutionContext)
    extends AppBaseController {

  def create(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val createTransactionRequest = validateJsonWithCaseClass[CreateTransactionRequest](request.body)
    val transactionId            = generateUniqueId
    logger.info(s"Creating transaction: $transactionId for request: ${request.toString()}")
    transactionsDAO
      .create(transactionId, createTransactionRequest)
      .map { transactionId =>
        Ok(
          Json.obj(
            "status"        -> 200,
            "description"   -> "Transaction created successfully",
            "transactionId" -> transactionId
          )
        )
      }
      .recoverWith {
        case ex: Exception =>
          logger.error(s"Failed request with ex: ${ex.getMessage}")
          Future.successful(InternalServerError(ex.getMessage))
      }
  }

  def getTransactionById(transactionId: String): Action[AnyContent] = Action.async {
    logger.info(s"Gettting transaction: $transactionId")
    transactionsDAO
      .getTransactionById(transactionId)
      .map { transaction =>
        Ok(
          Json.obj(
            "status"      -> 200,
            "description" -> "Transaction fetched successfully",
            "transaction" -> transaction
          )
        )
      }
      .recoverWith {
        case ex: Exception =>
          logger.error(s"Failed request with ex: ${ex.getMessage}")
          Future.successful(InternalServerError(ex.getMessage))
      }
  }

}
