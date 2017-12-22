import com.besuikerd.cmap.integration.playjson.CmapReads
import com.besuikerd.cmap.typeclass._
import org.scalatest.{FlatSpec, MustMatchers}
import play.api.libs.json._
import com.besuikerd.cmap.integration.playjson.CmapReads._
import cats.implicits._

class JsValueCatamorphismsSpec extends FlatSpec with MustMatchers {

  "JsValue catamorphism" must "count the number of string instances in a given json value" in {
    implicit val addressFormat: Format[Address] = Json.format[Address]
    implicit val personFormat: Format[Person]   = Json.format[Person]

    val countStrings: FAlgebra[JsValueF, Int] = {
      case JsNullF()         => 0
      case JsBooleanF(value) => 0
      case JsNumberF(value)  => 0
      case JsStringF(value)  => 1
      case JsArrayF(values)  => values.sum
      case JsObjectF(fields) => fields.values.sum
    }

    val person     = Person("John", "Doe", 42, Address("Baker Street", 123))
    val personJson = personFormat.writes(person)

    val x = new CmapCata(countStrings)

    x.runCmap(personJson) mustBe Right(3)

    val mightFail: FAlgebra[JsValueF, CmapReads[Int]] = {
      case JsNullF()         => CmapReads.success(1)
      case JsBooleanF(value) => CmapReads.fail("booleans are not allowed here!")
      case JsNumberF(value)  => CmapReads.success(2)
      case JsStringF(value)  => CmapReads.success(3)
      case JsArrayF(values)  => CmapReads.all(values).map(t => t.sum)
      case JsObjectF(fields) => CmapReads.success(42)
    }

    val t = CmapReads.cata(mightFail)

    t.join.runCmap(JsTrue) mustBe Left(List((JsPath(), List(JsonValidationError("booleans are not allowed here!")))))
  }
}
