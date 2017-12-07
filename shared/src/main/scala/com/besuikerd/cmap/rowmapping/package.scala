package com.besuikerd.cmap

import java.time.{LocalDate, LocalTime}

package object rowmapping {
  trait RowLookup {
    def apply[T](matcher: ColumnMatcher)(implicit cellType: CellType[T]): Either[RowMappingError, T]
  }

  sealed trait ColumnMatcher

  case class ByPredicate(predicate: ColumnPredicate) extends ColumnMatcher
  case class ByName(name: String)                    extends ColumnMatcher

  trait CellTransformation {
    def transformInt: Either[RowTransformError, Int]         = transformDynamic
    def transformString: Either[RowTransformError, String]   = transformDynamic
    def transformBoolean: Either[RowTransformError, Boolean] = transformDynamic
    def transformDouble: Either[RowTransformError, Double]   = transformDynamic
    def transformDate: Either[RowTransformError, LocalDate]  = transformDynamic
    def transformTime: Either[RowTransformError, LocalTime]  = transformDynamic

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
