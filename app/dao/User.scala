package dao

import models._
import play.api.Configuration
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

  def getUserIdByvalidId(validId: String): Future[Either[SQLException, List[String]]] = crud.select(
    tableName = tableName,
    Some("id"),
    Some(s"where validId = \'$validId\'")
  )

//  def getUserById(id: String): Future[Either[SQLException, Int]] = crud.select(
//      tableName = tableName,
//      condition = Some(s"where id = \'$id\'")
//    )
//
//  def getAllUsers(limit: Int = 50): Future[Either[SQLException, Int]] = crud.select(
//    tableName = tableName,
//    condition = Some(s"limit $limit")
//  )

}
