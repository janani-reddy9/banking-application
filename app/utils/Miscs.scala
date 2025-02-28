package utils

import play.api.libs.json.{JsValue, Reads}

object Miscs {

  def generateUniqueId: String = java.util.UUID.randomUUID().toString

  def validateJsonWithCaseClass[T](json: JsValue)(implicit rd: Reads[T]): T = {
    json.validateOpt[T].map(x => if(x.isEmpty) throw new Exception("Invalid Input"))
    json.as[T]
  }

}
