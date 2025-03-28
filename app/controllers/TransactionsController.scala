package controllers

import dao.TransactionsDAO
import models.CreateTransactionRequest
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import utils.Miscs._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TransactionsController @Inject()(val controllerComponents: ControllerComponents, transactionsDAO: TransactionsDAO)(implicit ec: ExecutionContext) extends AppBaseController {

  def create(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val createTransactionRequest = validateJsonWithCaseClass[CreateTransactionRequest](request.body)
    val transactionId = generateUniqueId
    logger.info(s"Creating transaction: $transactionId for request: ${request.toString()}")
    transactionsDAO.create(transactionId, createTransactionRequest).map { transactionId =>
      Ok(
        Json.obj(
          "status" -> 200,
          "description" -> "Transaction created successfully",
          "transactionId" -> transactionId
        )
      )
    }.recoverWith {
      case ex: Exception =>
        logger.error(s"Failed request with ex: ${ex.getMessage}")
        Future.successful(InternalServerError(ex.getMessage))
    }
  }

  def getTransactionById(transactionId: String): Action[AnyContent] = Action.async {
    logger.info(s"Gettting transaction: $transactionId")
    transactionsDAO.getTransactionById(transactionId).map { transaction =>
      Ok(
        Json.obj(

          "status" -> 200,
          "description" -> "Transaction fetched successfully",
          "transaction" -> transaction
        )
      )
    }.recoverWith {
      case ex: Exception =>
        logger.error(s"Failed request with ex: ${ex.getMessage}")
        Future.successful(InternalServerError(ex.getMessage))
    }
  }

}
