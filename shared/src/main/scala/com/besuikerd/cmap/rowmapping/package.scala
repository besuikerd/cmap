package com.besuikerd.cmap

import java.time.{Instant, LocalDate, LocalDateTime, LocalTime}

package object rowmapping {
  trait RowLookup {
    def apply[T](matcher: ColumnMatcher)(implicit cellType: CellType[T]): Either[RowMappingError, T]
  }

  sealed trait ColumnMatcher

  case class ByPredicate(predicate: ColumnPredicate) extends ColumnMatcher
  case class ByName(name: String)                    extends ColumnMatcher

  trait CellTransformation {
    def transformInt: Either[RowTransformError, Int]                = transformDynamic
    def transformString: Either[RowTransformError, String]          = transformDynamic
    def transformBoolean: Either[RowTransformError, Boolean]        = transformDynamic
    def transformDouble: Either[RowTransformError, Double]          = transformDynamic
    def transformInstant: Either[RowTransformError, Instant]        = transformDynamic
    def transformDate: Either[RowTransformError, LocalDate]         = transformDynamic
    def transformTime: Either[RowTransformError, LocalTime]         = transformDynamic
    def transformDateTime: Either[RowTransformError, LocalDateTime] = transformDynamic

    def transformDynamic[T]: Either[RowTransformError, T] =
      Left(RowTransformError("No transformation for given type implemented"))
  }

  trait CellType[T] {
    def transformation: CellTransformation => Either[RowTransformError, T]
  }

  object CellType {
    implicit object CellTypeInt extends CellType[Int] {
      override def transformation = _.transformInt
    }
    implicit object CellTypeString extends CellType[String] {
      override def transformation = _.transformString
    }

    implicit object CellTypeBoolean extends CellType[Boolean] {
      override def transformation = _.transformBoolean
    }

    implicit object CellTypeDouble extends CellType[Double] {
      override def transformation = _.transformDouble
    }

    implicit object CellTypeInstant extends CellType[Instant] {
      override def transformation = _.transformInstant
    }

    implicit object CellTypeLocalTime extends CellType[LocalTime] {
      override def transformation = _.transformTime
    }

    implicit object CellTypeLocalDate extends CellType[LocalDate] {
      override def transformation = _.transformDate
    }

    implicit object CellTypeLocalDateTime extends CellType[LocalDateTime] {
      override def transformation = _.transformDateTime
    }
  }

  type ColumnPredicate = String => Boolean

  sealed trait RowMappingError
  case class ColumnNotFound(column: String)   extends RowMappingError
  case class NoMatchingColumnFound()          extends RowMappingError
  case class RowTransformError(cause: String) extends RowMappingError

  type Context       = RowLookup
  type Error         = List[RowMappingError]
  type RowMapping[T] = Cmap[Context, Error, T]
}
