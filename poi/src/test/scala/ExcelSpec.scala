import com.besuikerd.cmap.poi.{ExcelParseResult, ExcelParser}
import org.scalatest.{FlatSpec, MustMatchers}

class ExcelSpec extends FlatSpec with MustMatchers {
  val parser = ExcelParser.apply(PersonMapping.personMapping)

  "ExcelParser" should "parse an excel file with valid person records" in {
    val resource = "/persons.xlsx"

    val ExcelParseResult(errors, persons) = parser.parseExcel(getClass.getResourceAsStream(resource))

    persons.size mustBe 2

    persons(0) mustBe Person(
      name = "John",
      surname = "Doe",
      age = 42,
      address = Address(
        street = "Baker Street",
        number = 123
      )
    )

    persons(1) mustBe Person(
      name = "Jane",
      surname = "Doe",
      age = 44,
      address = Address(
        street = "West Street",
        number = 456
      )
    )

  }

}
