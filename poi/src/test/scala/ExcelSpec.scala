import java.time._

import cats.implicits._
import com.besuikerd.cmap.poi.{ExcelParseResult, ExcelParser}
import com.besuikerd.cmap.rowmapping.RowMapping
import org.scalatest.{FlatSpec, MustMatchers}

class ExcelSpec extends FlatSpec with MustMatchers {
  import RowMapping._

  "ExcelParser" should "parse an excel file with valid person records" in {

    val resource = "/persons.xlsx"

    val parser                            = ExcelParser.apply(PersonMapping.personMapping)
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

  it should "process dates correctly" in {
    val resource = "/dates.xlsx"

    val rowMapping = RowMapping(
      byName[LocalDate]("date"),
      byName[LocalTime]("time"),
      byName[Instant]("datetime"),
      byName[LocalDateTime]("datetime")
    )
    val parser = ExcelParser(rowMapping)

    val ExcelParseResult(errors, tpl) = parser.parseExcel(getClass.getResourceAsStream(resource))

    tpl.size mustBe 1
    errors.size mustBe 0

    val (date, time, instant, datetime) = tpl(0)

    date.getYear mustBe 2017
    date.getMonth mustBe Month.MARCH
    date.getDayOfMonth mustBe 3

    time.getHour mustBe 20
    time.getMinute mustBe 0

    datetime.toInstant(ZoneId.systemDefault().getRules.getOffset(datetime)) mustBe instant
    datetime.getYear mustBe 2017
    datetime.getMonth mustBe Month.APRIL
    datetime.getDayOfMonth mustBe 4
    datetime.getDayOfMonth mustBe 4
  }

}
