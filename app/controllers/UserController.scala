package controllers

import dao.UserDAO
import models.{CreateUserRequest, UpdateUserRequest}
import org.apache.pekko.Done
import play.api.libs.json.{JsArray, JsString, JsValue, Json}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import utils.Miscs._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserController @Inject()(val controllerComponents: ControllerComponents, userDAO: UserDAO)(implicit ec: ExecutionContext) extends AppBaseController {

  def createBankUser(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val createUserRequest = validateJsonWithCaseClass[CreateUserRequest](request.body)
    val userId = generateUniqueId
    logger.info(s"Creating user with id: $userId for request: ${request.toString()}")
    userDAO.createUser(createUserRequest, userId).map {
      x =>
        Ok(
          Json.obj(
            "status" -> 200,
            "description" -> "User created successfully",
            "id" -> s"$userId",
            "rows_inserted" -> x
          )
        )
    }.recoverWith {
      case ex =>
        logger.error(s"Failed request with ex: ${ex.getMessage}")
        Future.successful(InternalServerError(ex.getMessage))
    }
  }

  def getBankUserByValidId(id: String): Action[AnyContent] = Action.async {
    logger.info(s"Getting user with id: $id")
    userDAO.getUserIdByvalidId(id).map{
      rows => Ok(
        Json.obj(
          "status" -> 200,
          "description" -> "User retrieved successfully",
          "id" -> JsString(rows.headOption.getOrElse("Doesn't exist"))
        )
      )
    }.recoverWith {
      case ex: Exception =>
        logger.error(s"Failed request with ex: ${ex.getMessage}")
        Future.successful(InternalServerError(ex.getMessage))
    }
  }

  def getBankUsers(): Action[AnyContent] = Action.async {
    logger.info(s"Getting all users")
    userDAO.getUsers().map {
      rows => Ok(
        Json.obj(
          "status" -> 200,
          "description" -> "Users retrieved successfully",
          "users" -> JsArray(rows.map(row => Json.parse(row.toJsonString)))
        )
      )
    }.recoverWith {
      case ex: Exception =>
        logger.error(s"Failed request with ex: ${ex.getMessage}")
        Future.successful(InternalServerError(ex.getMessage))
    }
  }

  def login(userId: String, password: String): Action[AnyContent] = Action.async {
    logger.info(s"Logging in for $userId")
    userDAO.getSessionId(userId, password).map {
      case None => BadRequest("Invalid credentials")
      case Some(sessionId) => Ok(
        Json.obj(
          "status" -> 200,
          "description" -> "User logged in successfully",
          "sessionId" -> sessionId
        )
      )
    }.recoverWith {
      case ex =>
        logger.error(s"Failed request with ex: ${ex.getMessage}")
        Future.successful(InternalServerError(ex.getMessage))
    }
  }

  def logout(userId: String): Action[AnyContent] = Action.async {
    logger.info(s"Logging out for $userId")
    userDAO.removeSessionId(userId).map {
      case Done => Ok(
        Json.obj(
          "status" -> 200,
          "description" -> "User logged out successfully"
        )
      )
    }.recoverWith {
      case ex =>
        logger.error(s"Failed request with ex: ${ex.getMessage}")
        Future.successful(InternalServerError(ex.getMessage))
    }
  }

  def updateUser(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val updateUserRequest = Json.parse(request.body.toString()).as[UpdateUserRequest]
    logger.info(s"Updating user with id: ${updateUserRequest.userId} request: ${request.toString()}")
    userDAO.updateUser(updateUserRequest.userId, updateUserRequest.sessionId, updateUserRequest.updates).map {
      updatedRows => Ok(
        Json.obj(
          "status" -> 200,
          "description" -> "User updated successfully",
          "updatedUsersCount" -> updatedRows
        )
      )
    }.recoverWith {
      case ex =>
        logger.error(s"Failed request with ex: ${ex.getMessage}")
        Future.successful(InternalServerError(ex.getMessage))
    }
  }

}
