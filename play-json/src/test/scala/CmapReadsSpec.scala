import com.besuikerd.cmap.integration.playjson.CmapReads
import org.scalatest.{EitherValues, FlatSpec, MustMatchers}
import play.api.libs.json._

class CmapReadsSpec extends FlatSpec with MustMatchers with EitherValues {
  implicit val addressFormat: Format[Address] = Json.format[Address]
  implicit val personFormat: Format[Person]   = Json.format[Person]
  import CmapReads._

  "CmapReads" should "wrap Json Reads" in {
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

  it should "collect multiple validation errors" in {

    val json = JsObject(
      Map(
        "key1" -> JsString("val1"),
        "key2" -> JsString("val2")
      ))

    val path1 = JsPath \ "wrongkey1"
    val path2 = JsPath \ "wrongkey2"

    val cmap = CmapReads.apply[String, String](
      path1,
      path2
    )

    val result = cmap.runCmap(json)

    result mustBe 'left
    val Left(errors) = result
    errors.size mustBe 2

    errors(0)._1 mustBe path1
    errors(0)._2.head.message mustBe "error.path.missing"

    errors(1)._1 mustBe path2
    errors(1)._2.head.message mustBe "error.path.missing"
  }

}
