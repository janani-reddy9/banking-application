package dao

import models._
import play.api.Configuration
import slick.jdbc.GetResult

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AccountsDAO  @Inject()(configuration: Configuration, crud: CRUD, userDAO: UserDAO)(implicit ec: ExecutionContext) {

  val accountsTableName: String = configuration.get[String]("table.account")

  def createAccount(accountId: String, request: AccountCreateRequest): Future[Int] = {
    for {
      sessionValidity <- userDAO.isSessionValid(request.userId, request.sessionId)
      _ = if (!sessionValidity) throw new Exception("Invalid session")
      accountType <- getAccountTypeName(request.accountTypeId)
      _ = (accountType, request.userIds.size) match {
        case (Single, size) if size != 0 => throw new Exception("Only one user is allowed")
        case (Joint, size) if size > 1 => throw new Exception("Only two users are allowed")
        case (_, _) => "Do nothing, because other invalid cases are handled. So, let the flow go on!"
      }
      valuesToInsertInAccTbl = Seq(s"\'$accountId\'", s"\'${request.accountTypeId}\'", request.balance).mkString(",")
      insertToAccTblRes <- crud.insert(accountsTableName, valuesToInsertInAccTbl)
      _ <- insertToMappingTbl(request.accountTypeId, request.userId, accountId, request.userIds)
    } yield insertToAccTblRes
  }

  private def getAccountTypeName(accountTypeId: String): Future[AccountTypes] = {
    crud
      .select[String]("account_type", Some("name"), Some(s"WHERE id = \'$accountTypeId\'"))
      .map {
        valueList => valueList.head match {
          case "single" => Single
          case "joint" => Joint
          case acc => throw new Exception(s"Invalid account type - $acc")
        }
      }
  }

  private def insertToMappingTbl(accountTypeId: String, userId: String, accountId: String, userIds: List[String]): Future[Int] = {
    if (accountTypeId == "1") {
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
      )(GetResult(r => AccountTable(r.nextString(), r.nextString(), r.nextDouble())))
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
      Account(accountId = id, userIds = users, accountType = accountType.toString, accountTypeId = accountInfo.head.accountTypeId, balance = accountInfo.head.balance)
    }
  }

  def isAccountValid(accountId: String): Future[Boolean] = {
    crud
      .select[Int](tableName = accountsTableName, columnsToRetrive = Some("count(*)"), condition = Some(s"WHERE id=\'$accountId\'"))
      .map(columns => if(columns.head == 1) true else false)
  }

}
