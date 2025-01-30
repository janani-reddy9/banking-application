package models

import play.api.libs.json.{Json, OFormat}

object CreateUserRequest {
  implicit val jsonFormat: OFormat[CreateUserRequest] = Json.format[CreateUserRequest]
}

case class CreateUserRequest(name: String, phoneNumber: String, validId: String, email: String, address: String, password: String)

case class CreateUserResponse(id: String, status: Int, description: String)

case class FieldDetails(key: String, value: String)

case class UpdateUserRequest(userId: String, sessionId: String, updates: Seq[FieldDetails])

case class User(id: String, validId: String,  name: String, email: String, address: String, phoneNumber: String) {
  def toJsonString: String = Json.obj(
    "id" -> id,
    "validId" -> validId,
    "name" -> name,
    "email" -> email,
    "address" -> address,
    "phoneNumber" -> phoneNumber
  ).toString()
}

