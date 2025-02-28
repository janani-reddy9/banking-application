import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{Json, OFormat}
import utils.Miscs

class MiscsSpec extends PlaySpec {

  "Miscs" should {
    "validate for right json" in {
      val request = Json.parse(
        """
          |{
          |  "value1": 1,
          |  "value2": "value2"
          |}
          |""".stripMargin
      )
      Miscs.validateJsonWithCaseClass[Test](request) mustEqual Test(Some(1), "value2")
    }

    "throw error" in {
      val request = Json.parse(
        """
          |{
          |  "value1": "a",
          |  "value2": "4"
          |}
          |""".stripMargin
      )
      assertThrows[Exception](Miscs.validateJsonWithCaseClass[Test](request))
    }
  }

}

case class Test(value1: Option[Int], value2: String)

object Test {
  implicit val testFormat: OFormat[Test] = Json.format[Test]
}
