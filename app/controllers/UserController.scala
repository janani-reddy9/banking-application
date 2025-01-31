package controllers

import dao.UserDAO
import models.{CreateUserRequest, DeactivateUserRequest, UpdateUserRequest}
import org.apache.pekko.Done
import play.api.libs.json.{JsArray, JsString, Json}
import play.api.mvc.{BaseController, ControllerComponents}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserController @Inject()(val controllerComponents: ControllerComponents, user: UserDAO)(implicit ec: ExecutionContext) extends BaseController {

  def createBankUser = Action.async(parse.json) { implicit request =>
    val createUserRequest = Json.parse(request.body.toString()).as[CreateUserRequest]
    user.createUser(createUserRequest).map {
      case Left(e) => BadRequest(e.getMessage)
      case Right(_) => Ok(
        Json.obj(
          "status" -> 200,
          "description" -> "User created successfully"
        )
      )
    }.recoverWith {
      case ex => Future.successful(InternalServerError(ex.getMessage))
    }
  }

  def getBankUserByValidId(id: String) = Action.async {
    user.getUserIdByvalidId(id).map{
      case Left(e) => BadRequest(e.getMessage)
      case Right(rows) => Ok(
        Json.obj(
          "status" -> 200,
          "description" -> "User retrieved successfully",
          "id" -> JsString(rows.headOption.getOrElse("Doesn't exist"))
        )
      )
    }.recoverWith {
      case ex: Exception => Future.successful(InternalServerError(ex.getMessage))
    }
  }

  def getBankUsers() = Action.async {
    user.getUsers().map {
      case Left(e) => BadRequest(e.getMessage)
      case Right(rows) => Ok(
        Json.obj(
          "status" -> 200,
          "description" -> "Users retrieved successfully",
          "users" -> JsArray(rows.map(row => Json.parse(row.toJsonString)))
        )
      )
    }.recoverWith {
      case ex: Exception => Future.successful(InternalServerError(ex.getMessage))
    }
  }

  def login(userId: String, password: String) = Action.async {
    user.getSessionId(userId, password).map {
      case None => BadRequest("Invalid credentials")
      case Some(sessionId) => Ok(
        Json.obj(
          "status" -> 200,
          "description" -> "User logged in successfully",
          "sessionId" -> sessionId
        )
      )
    }.recoverWith {
      case ex => Future.successful(InternalServerError(ex.getMessage))
    }
  }

  def logout(userId: String) = Action.async {
    user.removeSessionId(userId).map {
      case Done => Ok(
        Json.obj(
          "status" -> 200,
          "description" -> "User logged out successfully"
        )
      )
    }.recoverWith {
      case ex => Future.successful(InternalServerError(ex.getMessage))
    }
  }

  def updateUser = Action.async(parse.json) { implicit request =>
    val updateUserRequest = Json.parse(request.body.toString()).as[UpdateUserRequest]
    user.updateUser(updateUserRequest.userId, updateUserRequest.sessionId, updateUserRequest.updates).map {
      case Left(e) => BadRequest(e.getMessage)
      case Right(_) => Ok(
        Json.obj(
          "status" -> 200,
          "description" -> "User updated successfully"
        )
      )
    }.recoverWith {
      case ex => Future.successful(InternalServerError(ex.getMessage))
    }
  }

}
