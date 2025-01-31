package dao

import models._
import org.apache.pekko.Done
import play.api.Configuration
import play.api.cache.{AsyncCacheApi, NamedCache}
import slick.jdbc.GetResult
import utils.Miscs.generateUniqueId

import java.sql.SQLException
import javax.inject.Inject
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

class UserDAO @Inject()(@NamedCache("session-cache") sessionCache: AsyncCacheApi, configuration: Configuration, crud: CRUD)(implicit ec: ExecutionContext) {

  val tableName = configuration.get[String]("table.user")

  def createUser(createRequest: CreateUserRequest): Future[Either[Exception,Int]] = {
    val valuesToInsert = Seq(generateUniqueId, createRequest.validId, createRequest.name, createRequest.password, createRequest.email, createRequest.address, createRequest.phoneNumber).map(column => s"\'$column\'").mkString(",")
    crud.insert(tableName, valuesToInsert)
  }

  def getUserIdByvalidId(validId: String): Future[Either[SQLException, List[String]]] =
    crud.select[String](
      tableName = tableName,
      Some("id"),
      Some(s"where validId = \'$validId\'")
    )(GetResult(r => r.nextString()))

  def getUsers(limit: Int = 50): Future[Either[SQLException, List[models.User]]] =
    crud.select[models.User](
      tableName = tableName,
      condition = Some(s"limit $limit")
    )(GetResult(r => models.User(r.nextString(), r.nextString(), r.nextString(), r.nextString(), r.nextString(), r.nextString(), r.nextBoolean())))

  def updateUser(userId: String, sessionId: String, updates: Seq[FieldDetails]): Future[Either[Exception,Int]] = {
    isSessionValid(userId, sessionId).flatMap {
      case true =>
        val valuesToUpdate = updates.map(field => s"${field.key} = \'${field.value}\'").mkString(",")
        crud.update(tableName, valuesToUpdate, s"id = \'$userId\'")
      case false =>
        Future(Left(new Exception("Invalid session")))
    }
  }

  def isSessionValid(userId: String, sessionId: String): Future[Boolean] = {
    val sessionIdFut = sessionCache.get[String](userId)
    sessionIdFut.map(id => if(id.isDefined && id.get.equals(sessionId)) true else false)
  }

  def getSessionId(userId: String, password: String): Future[Option[String]] = {
    val retrieveByUserIdAndPassword = crud.select[Int](
      tableName = tableName,
      Some("count(*)"),
      Some(s"where id = \'$userId\' and password = \'$password\'")
    )(GetResult(r => r.nextInt()))
    val sessionIdFut = for{
      createSession <- retrieveByUserIdAndPassword.map{
      case Left(_) => false
      case Right(result) => if(result.size == 1) true else false
      }
      sessionId <- Future(if(createSession) Some(generateUniqueId) else None)
    } yield sessionId
    val _ = sessionIdFut.map(sessionId => if(sessionId.isDefined) sessionCache.set(userId, sessionId.get, 5.minutes))
    sessionIdFut
  }

  def removeSessionId(userId: String): Future[Done] = sessionCache.remove(userId)

}
