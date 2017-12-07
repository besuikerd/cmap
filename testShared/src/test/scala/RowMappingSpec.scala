import com.besuikerd.cmap.rowmapping._
import org.scalatest.{EitherValues, FlatSpec, MustMatchers}

class RowMappingSpec extends FlatSpec with EitherValues with MustMatchers {

  class TestRowMappingContext(row: (String, String)*) extends RowLookup {
    val map = Map(row: _*)

    class TestCellTransformation(value: String) extends CellTransformation {
      override def transformInt: Either[RowTransformError, Int] =
        try {
          Right(value.toInt)
        } catch {
          case e: NumberFormatException => Left(RowTransformError(s"Could not transform $value to Int"))
        }

      override def transformString: Either[RowTransformError, String] = Right(value)
    }

    override def apply[T](matcher: ColumnMatcher)(implicit cellType: CellType[T]): Either[RowMappingError, T] =
      matcher match {
        case ByName(name) =>
          map.get(name) match {
            case Some(value) => cellType.transformation(new TestCellTransformation(value))
            case None        => Left(ColumnNotFound(name))
          }
        case ByPredicate(pred) =>
          map.find { case (k, v) => pred(k) } match {
            case Some((k, v)) => cellType.transformation(new TestCellTransformation((v)))
            case None         => Left(NoMatchingColumnFound())
          }
      }
  }

  "RowMapping" should "map a valid person record to a person" in {
    val ctx = new TestRowMappingContext(
      "name"          -> "John",
      "surname"       -> "Doe",
      "age"           -> "42",
      "street_name"   -> "Baker Street",
      "street_number" -> "123"
    )

    val result = PersonMapping.personMapping.apply(ctx)
    result mustBe Right(
      Person("John", "Doe", 42, Address("Baker Street", 123))
    )
  }
}
