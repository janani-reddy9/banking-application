package models

import play.api.libs.json.{Json, OFormat}

object Account {
  implicit val accountFormat: OFormat[Account] = Json.format[Account]
}

case class Account(accountId: String, userIds: List[String], accountType: String, accountTypeId: String, balance: Double)

case class AccountTable(accountId: String, accountTypeId: String, balance: Double)

case class AccountCreateRequest(userId: String, sessionId: String, userIds: List[String] = List.empty, accountTypeId: String = "1", balance: Double = 1000)

object AccountCreateRequest {
  implicit val accountCreateRequestFormat: OFormat[AccountCreateRequest] = Json.format[AccountCreateRequest]
}

case class AccountUserMapping(accountId: String, userId: String)


sealed trait AccountTypes

case object Joint extends AccountTypes
case object Single extends AccountTypes
