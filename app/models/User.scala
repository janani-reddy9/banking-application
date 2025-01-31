package models

import play.api.libs.json.{Json, OFormat}

object CreateUserRequest {
  implicit val jsonFormat: OFormat[CreateUserRequest] = Json.format[CreateUserRequest]
}

case class CreateUserRequest(name: String, phoneNumber: String, validId: String, email: String, address: String, password: String)

object FieldDetails {
  implicit val jsonFormat: OFormat[FieldDetails] = Json.format[FieldDetails]
}
case class FieldDetails(key: String, value: String)

object UpdateUserRequest {
  implicit val jsonFormat: OFormat[UpdateUserRequest] = Json.format[UpdateUserRequest]
}

case class UpdateUserRequest(userId: String, sessionId: String, updates: Seq[FieldDetails])

case class User(id: String, validId: String,  name: String, email: String, address: String, phoneNumber: String, isActive: Boolean) {
  def toJsonString: String = Json.obj(
    "id" -> id,
    "validId" -> validId,
    "name" -> name,
    "email" -> email,
    "address" -> address,
    "phoneNumber" -> phoneNumber,
    "isActive" -> isActive
  ).toString()
}

case class DeactivateUserRequest(userId: String, sessionId: String, deactivationReason: String)