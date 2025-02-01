package dao

import models.Account
import play.api.Configuration
import slick.jdbc.GetResult
import utils.Miscs

import java.sql.SQLException
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AccountsDAO  @Inject()(configuration: Configuration, crud: CRUD, userDAO: UserDAO)(implicit ec: ExecutionContext) {

  val accountsTableName: String = configuration.get[String]("table.account")

  def createAccount(userId: String, sessionId: String, userIds: List[String], accountTypeId: Int, balance: Double): Future[Either[Exception, Int]] = {
    for {
      sessionValidity <- userDAO.isSessionValid(userId, sessionId)
      _ = if (!sessionValidity) throw new Exception("Invalid session")
      accountType <- getAccountTypeName(accountTypeId)
      _ = if (accountType == "single" && userIds.nonEmpty) throw new Exception("Only one user is allowed")
         else if(accountType == "joint" && userIds.size != 1) throw new Exception("Only two users are allowed")
      accountId = Miscs.generateUniqueId
      valuesToInsertInAccTbl = Seq(s"\'$accountId\'", s"\'$accountTypeId\'", balance).mkString(",")
      insertToAccTblRes <- crud.insert(accountsTableName, valuesToInsertInAccTbl)
      _ <- insertToMappingTbl(accountTypeId, userId, accountId, userIds)
    } yield insertToAccTblRes
  }

  private def getAccountTypeName(accountTypeId: Int): Future[String] = {
    crud
      .select[String]("account_type", Some("name"), Some(s"WHERE id = \'$accountTypeId\'"))
      .map{
        case Left(_) => throw new Exception("Account type is not valid")
        case Right(accountTypeName) => accountTypeName.headOption.get
      }
  }

  private def insertToMappingTbl(accountTypeId: Int, userId: String, accountId: String, userIds: List[String]): Future[Either[Exception, Int]] = {
    if (accountTypeId == 1) {
      val valuesToInsertInMapAccTbl = Seq(accountId, userId).map(value => s"\'$value\'").mkString(",")
      crud.insert("account_user_mapping", valuesToInsertInMapAccTbl)
    } else {
      val allUsers = userIds :+ userId
      val valuesToInsertInMapAccTbl = allUsers.map(userId => Seq(accountId, userId).map(value => s"\'$value\'").mkString(","))
      crud.insertAll("account_user_mapping", valuesToInsertInMapAccTbl)
    }
  }

  def getAccountById(id: String): Future[Either[SQLException, List[Account]]] = crud
      .select[Account](
        tableName = accountsTableName,
        condition = Some(s"id = \'$id\'")
      )(GetResult(r => Account(r.nextString(), r.nextString(), r.nextDouble())))

}
