package models

import play.api.libs.json.{Json, OFormat}

case class Account(accountId: String, accountType: String, balance: Double)

case class AccountCreateRequest(userId: String, sessionId: String, userIds: List[String] = List.empty, accountTypeId: Int = 1, balance: Double = 1000)

object AccountCreateRequest {
  implicit val accountCreateRequestFormat: OFormat[AccountCreateRequest] = Json.format[AccountCreateRequest]
}

case class AccountUserMapping(accountId: String, userId: String)
