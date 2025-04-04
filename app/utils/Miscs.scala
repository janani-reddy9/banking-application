package utils

import play.api.Logging
import play.api.libs.json.{JsValue, Reads}

object Miscs extends Logging {

  def generateUniqueId: String = java.util.UUID.randomUUID().toString

  def validateJsonWithCaseClass[T](json: JsValue)(implicit rd: Reads[T]): T = {
    json.validateOpt[T].map(x => if(x.isEmpty) {
      logger.error(s"Invalid Json for request")
      throw new Exception("Invalid Input")
    })
    logger.info(s"Json is valid")
    json.as[T]
  }

}
