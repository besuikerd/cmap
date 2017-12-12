import com.besuikerd.cmap.integration.playjson.CmapReads
import org.scalatest.{EitherValues, FlatSpec, MustMatchers}
import play.api.libs.json.{Format, JsPath, Json}

class CmapReadsSpec extends FlatSpec with MustMatchers with EitherValues {
  implicit val addressFormat: Format[Address] = Json.format[Address]
  implicit val personFormat: Format[Person]   = Json.format[Person]

  "CmapReads" should "wrap Json Reads" in {
    import CmapReads._

    val person = Person("John", "Doe", 42, Address("Baker Street", 123))

    val personJson = personFormat.writes(person)

    implicit val cmapAddress: CmapReads[Address] =
      CmapReads(Address.curried)(
        JsPath \ "street",
        JsPath \ "number"
      )

    val cmapPerson: CmapReads[Person] =
      CmapReads(Person.curried)(
        JsPath \ "name",
        JsPath \ "surname",
        JsPath \ "age",
        JsPath \ "address"
      )

    val result = cmapPerson.runCmap(personJson)
    result mustBe Right(person)
  }

}
