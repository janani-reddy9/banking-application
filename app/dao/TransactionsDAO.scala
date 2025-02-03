package dao

import models.{Account, Transaction}
import org.apache.pekko.Done
import play.api.Configuration
import play.api.cache.{AsyncCacheApi, NamedCache}
import slick.jdbc.GetResult

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TransactionsDAO @Inject()(@NamedCache("transactions-cache") transactionsCache: AsyncCacheApi, configuration: Configuration, crud: CRUD, userDAO: UserDAO, accountsDAO: AccountsDAO)(implicit ec: ExecutionContext) {

  val transactionTableName: String = configuration.get[String]("table.transaction")

  def create(transactionId: String, accountId: String, userId: String, sessionId: String, transactionType: String, amount: Double): Future[Int] = {
    val sessionvalidityFut = userDAO.isSessionValid(userId, sessionId)
    val accountValidityFut = accountsDAO.isAccountValid(accountId)
    val accountFut: Future[Account] = accountsDAO.getAccountById(accountId)
    val balance = for {
      sessionvalidity <- sessionvalidityFut
      accountValidity <- accountValidityFut
      transactionValidity <- isAnotherTransactionInProgress(formatTransactionsCacheKey(accountId))
      account <- accountFut
    } yield {
      require(sessionvalidity, "Invalid Session")
      require(accountValidity, "Account Doesn't exist")
      require(!transactionValidity, "Another Transaction is in progress")
      require(transactionType == "withdraw" || transactionType == "deposit", "Transaction type is not supported")
      (transactionType, account.balance) match {
        case ("withdraw", balance) if balance >= amount =>
          account.balance - amount
        case ("deposit", _) =>
          account.balance + amount
      }
    }
    val valuesToInsertInTransTbl = Seq(s"\'$transactionId\'", s"\'$accountId\'", s"\'$userId\'", s"\'$transactionType\'", amount, s"\'${System.currentTimeMillis()}\'").mkString(", ")
    balance.flatMap(
      value => {
        val key = formatTransactionsCacheKey(accountId)
        val _ = setTransactionCache(key, sessionId)
        val transaction = crud.insert(transactionTableName, valuesToInsertInTransTbl)
        transaction.flatMap(insertedRows => crud.update(accountsDAO.accountsTableName, s"balance = $value", s"id = \'$accountId\'"))
        val _ = removeTransactionCache(key)
        transaction
      }
    ).recoverWith {
      case e: Exception =>
        val key = formatTransactionsCacheKey(accountId)
        val _ = removeTransactionCache(key)
        throw e
    }
  }

  private def setTransactionCache(key: String, sessionId: String): Future[Done] = transactionsCache.set(key, sessionId)

  private def removeTransactionCache(key: String): Future[Done] = transactionsCache.remove(key)

  private def formatTransactionsCacheKey(accountId: String): String = s"$accountId"

  def getCache(key: String): Future[Option[String]] = transactionsCache.get[String](key)

  def isAnotherTransactionInProgress(key: String, count: Int = 3): Future[Boolean] = {
    getCache(key).flatMap {
      case Some(_) if count > 0 =>
        Thread.sleep(100)
        isAnotherTransactionInProgress(key, count - 1)
      case None => Future.successful(false)
      case _ => Future.successful(true)
    }
  }

  def getTransactionById(transactionId: String): Future[List[Transaction]] = crud
    .select[Transaction](transactionTableName, condition = Some(s"WHERE id=\'$transactionId\'"))(GetResult(r =>
      Transaction(r.nextString(), r.nextString(), r.nextString(), r.nextString(), r.nextString(), r.nextDouble(), r.nextString()))
    )
}
