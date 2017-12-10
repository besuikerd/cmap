package com.besuikerd.cmap.poi

import java.io.InputStream
import java.time.{LocalDate, LocalTime, ZoneId}
import java.util.{Calendar, Locale}

import com.besuikerd.cmap.poi.ExcelParser.HeaderIndex
import com.besuikerd.cmap.rowmapping
import com.besuikerd.cmap.rowmapping._
import org.apache.poi.ss.usermodel._
import org.apache.poi.ss.util.CellReference
import org.apache.poi.ss.usermodel.{CellType => PoiCellType}
import sun.util.calendar.BaseCalendar

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import scala.collection.JavaConverters._

trait ExcelParser {
  type Result

  def parseExcel(input: InputStream): ExcelParseResult[Result] = {
    implicit val df: DataFormatter = new DataFormatter(Locale.ENGLISH)
    val workbook = WorkbookFactory
      .create(input)

    implicit val evaluator: FormulaEvaluator = workbook.getCreationHelper.createFormulaEvaluator()

    val epr: ExcelParseResult[Result] = workbook
      .sheetIterator()
      .asScala
      .map(parseSheet)
      .toList
      .foldLeft(ExcelParseResult.empty[Result]) {
        case (acc, epr) =>
          acc ++ epr
      }
    resultPostProcessing(epr)
  }

  def parseExcelAsync(input: InputStream)(
      implicit executionContext: ExecutionContext): Future[ExcelParseResult[Result]] =
    Future {
      parseExcel(input)
    }(executionContext)

  def resultPostProcessing(result: ExcelParseResult[Result]): ExcelParseResult[Result] = result

  def parseSheet(sheet: Sheet)(implicit df: DataFormatter, evaluator: FormulaEvaluator): ExcelParseResult[Result] = {
    val rowIterator = sheet
      .rowIterator()
      .asScala

    locateHeaderIndex(rowIterator) match {
      case Failure(exception) =>
        ExcelParseResult(List(SheetParseError(sheet, List(s"Could not find header row"))), List.empty)
      case Success(headerMapping) =>
        processRows(rowIterator, headerMapping)
    }
  }

  def processRows(rowIterator: Iterator[Row], headerIndex: HeaderIndex)(
      implicit df: DataFormatter,
      evaluator: FormulaEvaluator): ExcelParseResult[Result] = {
    val (errors, results) =
      rowIterator
        .map(row => mapRow(headerIndex, row).left.map(errors => RowParseError(row, errors)))
        .toList
        .foldLeft(List.empty[RowParseError], List.empty[Result]) {
          case ((ls, rs), Left(l))  => (ls :+ l, rs)
          case ((ls, rs), Right(r)) => (ls, rs :+ r)
        }
    ExcelParseResult(errors, results)
  }

  def mapHeader(sheet: Sheet): Map[String, Int] = Map.empty

  def headerToIndex(headerRow: Row)(implicit df: DataFormatter, evaluator: FormulaEvaluator): Map[String, Int] = {
    headerRow
      .cellIterator()
      .asScala
      .map(cell => (cellToString(cell), cell.getColumnIndex))
      .toMap
  }

  def cellToString(cell: Cell)(implicit df: DataFormatter, evaluator: FormulaEvaluator): String =
    df.formatCellValue(cell, evaluator).trim

  def locateHeaderIndex(iterator: Iterator[Row])(implicit df: DataFormatter,
                                                 evaluator: FormulaEvaluator): Try[HeaderIndex] =
    Try(headerToIndex(iterator.next()))

  def mapRow(headerMapping: HeaderIndex, row: Row)(implicit df: DataFormatter,
                                                   evaluator: FormulaEvaluator): RowMapping[Result]#Result = {
    val rowLookup = new RowLookup {
      override def apply[T](matcher: ColumnMatcher)(implicit cellType: rowmapping.CellType[T]) = {
        val index = matcher match {
          case ByPredicate(predicate) =>
            headerMapping.find { case (k, i) => predicate(k) }.map(_._2).toRight(NoMatchingColumnFound())
          case ByName(name) => headerMapping.get(name).toRight(ColumnNotFound("Value"))
        }
        index.right.flatMap(index => {
          val cell = row.getCell(index)
          cellType.transformation(new ExcelCellTransformation(cell, df, evaluator))
        })
      }
    }
    mapping.apply(rowLookup)
  }

  def indexToString(row: Row, headerMapping: Map[String, Int], value: String)(
      implicit df: DataFormatter,
      evaluator: FormulaEvaluator): Option[String] =
    headerMapping.get(value).map(index => cellToString(row.getCell(index)))

  def mapping: RowMapping[Result]
}

class ExcelCellTransformation(val cell: Cell, val dataFormatter: DataFormatter, val formulaEvaluator: FormulaEvaluator)
    extends CellTransformation
    with FallBackExcelCellTransformation {
  override def transformInt = expectCellType(PoiCellType.NUMERIC) { cell.getNumericCellValue.toInt }

  override def transformString = expectCellType(PoiCellType.STRING) { cell.getStringCellValue }

  override def transformBoolean = expectCellType(PoiCellType.BOOLEAN) { cell.getBooleanCellValue }

  override def transformDouble = expectCellType(PoiCellType.NUMERIC) { cell.getNumericCellValue }

  override def transformInstant = expectCellType(PoiCellType.NUMERIC) { cell.getDateCellValue.toInstant }

  override def transformDate = expectCellType(PoiCellType.NUMERIC) {
    cell.getDateCellValue.toInstant.atZone(ZoneId.systemDefault()).toLocalDate
  }

  override def transformTime = expectCellType(PoiCellType.NUMERIC) {
    val date = cell.getDateCellValue
    LocalTime.of(date.getHours, date.getMinutes, date.getSeconds)
  }

  override def transformDateTime = {
    for {
      date <- transformDate.right
      time <- transformTime.right
    } yield date.atTime(time)
  }

  def expectCellType[T](expected: PoiCellType)(transform: => T): Either[RowTransformError, T] =
    if (expected != cell.getCellTypeEnum) {
      invalidCellType(expected, cell.getCellTypeEnum)
    } else {
      Right(transform)
    }

  def invalidCellType(expected: PoiCellType, actual: PoiCellType) =
    Left(RowTransformError(s"Expected cell type: $expected, actual: $actual"))

}

trait FallBackExcelCellTransformation extends CellTransformation { this: ExcelCellTransformation =>
  val cell: Cell

  abstract override def transformInt = {
    fallbackTransformation(super.transformInt) { s =>
      try {
        Right(s.toInt)
      } catch {
        case e: NumberFormatException => Left(RowTransformError(s"could not convert $s to Int"))
      }
    }
  }

  def fallbackTransformation[T](result: Either[RowTransformError, T])(
      f: String => Either[RowTransformError, T]): Either[RowTransformError, T] = {
    val cellValue = dataFormatter.formatCellValue(cell, formulaEvaluator)
    result.left.flatMap(_ => f(cellValue))
  }
}

case class ExcelParseResult[T](errors: List[ExcelParseError], results: List[T]) {
  def ++(other: ExcelParseResult[T]): ExcelParseResult[T] = //scalastyle:ignore method.name
    ExcelParseResult(errors ++ other.errors, results ++ other.results)
}

object ExcelParseResult {
  def empty[T]: ExcelParseResult[T] = ExcelParseResult[T](List.empty, List.empty)
}

sealed trait ExcelParseError {
  val message: String
}

case class SheetParseError(sheet: Sheet, errors: List[String]) extends ExcelParseError {
  override val message = s"[${sheet.getSheetName}] ${errors.mkString(", ")}"
}

case class RowParseError(row: Row, errors: List[RowMappingError]) extends ExcelParseError {
  override val message: String = {
    val rowNum    = row.getRowNum
    val sheet     = row.getSheet
    val sheetName = sheet.getSheetName
    s"[$sheetName!$rowNum] ${errors.mkString(", ")}"
  }
}

case class CellParseError(cell: Cell, theMessage: String) extends ExcelParseError {
  override lazy val message: String = {
    val row         = cell.getRow
    val rowNum      = row.getRowNum
    val sheet       = cell.getSheet
    val sheetName   = sheet.getSheetName
    val columnLabel = CellReference.convertNumToColString(cell.getColumnIndex)

    s"[$sheetName!$columnLabel$rowNum] $theMessage"
  }
}

object ExcelParser {
  type HeaderIndex = Map[String, Int]

  type Aux[T] = ExcelParser { type Result = T } //scalastyle:ignore

  def apply[T](m: RowMapping[T]): ExcelParser.Aux[T] =
    new ExcelParser {
      override type Result = T
      override def mapping: RowMapping[Result] = m
    }
}
