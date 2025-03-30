package dao

import javax.inject.Inject

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import models.Account
import models.CreateTransactionRequest
import models.Transaction
import org.apache.pekko.Done
import play.api.cache.AsyncCacheApi
import play.api.cache.NamedCache
import play.api.Configuration
import slick.jdbc.GetResult

class TransactionsDAO @Inject() (
    @NamedCache("transactions-cache") transactionsCache: AsyncCacheApi,
    configuration: Configuration,
    crud: CRUD,
    userDAO: UserDAO,
    accountsDAO: AccountsDAO
)(implicit ec: ExecutionContext) {

  val transactionTableName: String = configuration.get[String]("table.transaction")

  def create(transactionId: String, request: CreateTransactionRequest): Future[Int] = {
    val sessionvalidityFut          = userDAO.isSessionValid(request.userId, request.sessionId)
    val accountValidityFut          = accountsDAO.isAccountValid(request.accountId)
    val accountFut: Future[Account] = accountsDAO.getAccountById(request.accountId)
    val balance = for {
      sessionvalidity     <- sessionvalidityFut
      accountValidity     <- accountValidityFut
      transactionValidity <- isAnotherTransactionInProgress(formatTransactionsCacheKey(request.accountId))
      account             <- accountFut
    } yield {
      require(sessionvalidity, "Invalid Session")
      require(accountValidity, "Account Doesn't exist")
      require(!transactionValidity, "Another Transaction is in progress")
      require(
        request.transactionType == "withdraw" || request.transactionType == "deposit",
        "Transaction type is not supported"
      )
      (request.transactionType, account.balance) match {
        case ("withdraw", balance) if balance >= request.amount =>
          account.balance - request.amount
        case ("deposit", _) =>
          account.balance + request.amount
      }
    }
    val valuesToInsertInTransTbl = Seq(
      s"\'$transactionId\'",
      s"\'${request.accountId}\'",
      s"\'${request.userId}\'",
      s"\'${request.transactionType}\'",
      request.amount,
      s"\'${System.currentTimeMillis()}\'"
    ).mkString(", ")
    balance
      .flatMap(value => {
        val key         = formatTransactionsCacheKey(request.accountId)
        val _           = setTransactionCache(key, request.sessionId)
        val transaction = crud.insert(transactionTableName, valuesToInsertInTransTbl)
        transaction.flatMap(insertedRows =>
          crud.update(accountsDAO.accountsTableName, s"balance = $value", s"id = \'${request.accountId}\'")
        )
        val _ = removeTransactionCache(key)
        transaction
      })
      .recoverWith {
        case e: Exception =>
          val key = formatTransactionsCacheKey(request.accountId)
          val _   = removeTransactionCache(key)
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
      case _    => Future.successful(true)
    }
  }

  def getTransactionById(transactionId: String): Future[List[Transaction]] = crud
    .select[Transaction](transactionTableName, condition = Some(s"WHERE id=\'$transactionId\'"))(
      GetResult(r =>
        Transaction(
          r.nextString(),
          r.nextString(),
          r.nextString(),
          r.nextString(),
          r.nextString(),
          r.nextDouble(),
          r.nextString()
        )
      )
    )
}
