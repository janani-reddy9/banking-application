package dao

import models._
import play.api.Configuration
import slick.jdbc.{GetResult, PositionedResult}
import utils.Miscs.generateUniqueId

import java.sql.SQLException
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class User @Inject() (configuration: Configuration, crud: CRUD)(implicit ec: ExecutionContext) {

  val tableName = configuration.get[String]("table.user")

  def createUser(createRequest: CreateUserRequest): Future[Either[Exception,Int]] = {
    val valuesToInsert = Seq(generateUniqueId, createRequest.validId, createRequest.name, createRequest.password, createRequest.email, createRequest.address, createRequest.phoneNumber).map(column => s"\'$column\'").mkString(",")
    crud.insert(tableName, valuesToInsert)
  }

  def getUserIdByvalidId(validId: String): Future[Either[SQLException, List[String]]] = {
    println("started")
    crud.select[String](
      tableName = tableName,
      Some("id"),
      Some(s"where validId = \'$validId\'")
    )(GetResult(r => r.nextString()))
  }

  def getUsers(limit: Int = 50): Future[Either[SQLException, List[models.User]]] = {
    println("started")
    crud.select[models.User](
      tableName = tableName,
      condition = Some(s"limit $limit")
    )(GetResult(r => models.User(r.nextString(), r.nextString(), r.nextString(), r.nextString(), r.nextString(), r.nextString())))
  }

}
