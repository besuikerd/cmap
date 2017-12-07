package com.besuikerd.cmap.rowmapping

import com.besuikerd.cmap.{Cmap, CmapOps}

class ByMatcher[T](matcher: ColumnMatcher)(implicit cellType: CellType[T]) extends Cmap[Context, Error, T] {
  override def runCmap(ctx: Context) = ctx.apply(matcher).left.map(List(_))
}

object RowMapping extends CmapOps[Context, Error] {

  def strictEquals(value: String): ColumnPredicate     = _ == value
  def ignoreCaseEquals(value: String): ColumnPredicate = _.equalsIgnoreCase(value)

  def byMatcher[T](matcher: ColumnMatcher)(implicit cellType: CellType[T]): RowMapping[T] = new ByMatcher[T](matcher)
  def byName[T](value: String)(implicit cellType: CellType[T]): RowMapping[T]             = byMatcher(ByName(value))
  def byPredicate[T](columnPredicate: ColumnPredicate)(implicit cellType: CellType[T]): RowMapping[T] =
    byMatcher(ByPredicate(columnPredicate))

//  object Implicits {
//    import scala.language.implicitConversions
//    implicit def stringToMapping[T](value: String)(implicit obtain: Obtain[T]): RowMapping[T] = byName(value)
//    implicit def predicateMapping[T](predicate: ColumnPredicate)(implicit obtain: Obtain[T]): RowMapping[T] =
//      byPredicate(predicate)
//  }
}