package dao

import models.{Account, AccountTable, AccountUserMapping}
import play.api.Configuration
import slick.jdbc.GetResult

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AccountsDAO  @Inject()(configuration: Configuration, crud: CRUD, userDAO: UserDAO)(implicit ec: ExecutionContext) {

  val accountsTableName: String = configuration.get[String]("table.account")

  def createAccount(accountId: String, userId: String, sessionId: String, userIds: List[String], accountTypeId: Int, balance: Double): Future[Int] = {
    for {
      sessionValidity <- userDAO.isSessionValid(userId, sessionId)
      _ = if (!sessionValidity) throw new Exception("Invalid session")
      accountType <- getAccountTypeName(accountTypeId)
      _ = if (accountType == "single" && userIds.nonEmpty) throw new Exception("Only one user is allowed")
         else if(accountType == "joint" && userIds.size != 1) throw new Exception("Only two users are allowed")
      valuesToInsertInAccTbl = Seq(s"\'$accountId\'", s"\'$accountTypeId\'", balance).mkString(",")
      insertToAccTblRes <- crud.insert(accountsTableName, valuesToInsertInAccTbl)
      _ <- insertToMappingTbl(accountTypeId, userId, accountId, userIds)
    } yield insertToAccTblRes
  }

  private def getAccountTypeName(accountTypeId: Int): Future[String] = {
    crud
      .select[String]("account_type", Some("name"), Some(s"WHERE id = \'$accountTypeId\'"))
      .map{
        valueList => valueList.head
      }
  }

  private def insertToMappingTbl(accountTypeId: Int, userId: String, accountId: String, userIds: List[String]): Future[Int] = {
    if (accountTypeId == 1) {
      val valuesToInsertInMapAccTbl = Seq(accountId, userId).map(value => s"\'$value\'").mkString(",")
      crud.insert("account_user_mapping", valuesToInsertInMapAccTbl)
    } else {
      val allUsers = userIds :+ userId
      val valuesToInsertInMapAccTbl = allUsers.map(userId => Seq(accountId, userId).map(value => s"\'$value\'").mkString(","))
      crud.insertAll("account_user_mapping", valuesToInsertInMapAccTbl)
    }
  }

  def getAccountById(id: String): Future[Account] = {
    isAccountValid(id).map(valid => if(!valid) throw new Exception("Account id doesn't exist"))
    val accountData = crud
      .select[AccountTable](
        tableName = accountsTableName,
        condition = Some(s"WHERE id = \'$id\'")
      )(GetResult(r => AccountTable(r.nextString(), r.nextInt(), r.nextDouble())))
    val userAccountData = crud.select[AccountUserMapping](
      tableName = "account_user_mapping",
      condition = Some(s"WHERE account_id = \'$id\'")
    )(GetResult(r => AccountUserMapping(r.nextString(), r.nextString())))
    for {
      accountInfo <- accountData
      userAccountInfo <- userAccountData
      users = userAccountInfo.map(x => x.userId)
      accountType <- getAccountTypeName(accountTypeId = accountInfo.head.accountTypeId)
    } yield {
      Account(accountId = id, userIds = users, accountType = accountType, balance = accountInfo.head.balance)
    }
  }

  def isAccountValid(accountId: String): Future[Boolean] = {
    crud
      .select[Int](tableName = accountsTableName, columnsToRetrive = Some("count(*)"), condition = Some(s"WHERE id=\'$accountId\'"))
      .map(columns => if(columns.head != 1) true else false)
  }

}
