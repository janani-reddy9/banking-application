package models

import play.api.libs.json.Json
import play.api.libs.json.OFormat

case class CreateTransactionRequest(
    accountId: String,
    userId: String,
    sessionId: String,
    transactionType: String,
    amount: Double
)

object CreateTransactionRequest {
  implicit val createTransactionRequestFormat: OFormat[CreateTransactionRequest] = Json.format[CreateTransactionRequest]
}

case class Transaction(
    transactionId: String,
    accountId: String,
    userId: String,
    sessionId: String,
    transactionType: String,
    amount: Double,
    time: String
)

object Transaction {
  implicit val transactionFormat: OFormat[Transaction] = Json.format[Transaction]
}
