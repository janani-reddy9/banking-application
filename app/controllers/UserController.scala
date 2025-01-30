package controllers

import dao.User
import models.CreateUserRequest
import play.api.libs.json.{JsArray, JsString, Json}
import play.api.mvc.{BaseController, ControllerComponents}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserController @Inject()(val controllerComponents: ControllerComponents, user: User)(implicit ec: ExecutionContext) extends BaseController {

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

  def getBankUserByValidId(id: String) = Action.async { implicit request =>
    user.getUserIdByvalidId(id).map{
      case Left(e) =>
        println("ended - 404")
        BadRequest(e.getMessage)
      case Right(rows) =>
        println(s"ended - 200 ${rows.length}")
        Ok(
        Json.obj(
          "status" -> 200,
          "description" -> "User retrieved successfully",
          "id" -> JsString(rows.headOption.getOrElse("Doesn't exist"))
        )
      )
    }.recoverWith {
      case ex: Exception =>
        println(s"ended ${ex.getMessage}")
        Future.successful(InternalServerError(ex.getMessage))
    }
  }

  def getBankUsers() = Action.async { implicit request =>
    user.getUsers().map {
      case Left(e) =>
        println("ended - 404")
        BadRequest(e.getMessage)
      case Right(rows) =>
        println(s"ended - 200 ${rows.head.toString}")
        Ok(
        Json.obj(
          "status" -> 200,
          "description" -> "Users retrieved successfully",
          "users" -> JsArray(rows.map(row => Json.parse(row.toJsonString)))
        )
      )
    }.recoverWith {
      case ex: Exception =>
        println(s"ended ${ex.getMessage}")
        Future.successful(InternalServerError(ex.getMessage))
    }
  }


}
