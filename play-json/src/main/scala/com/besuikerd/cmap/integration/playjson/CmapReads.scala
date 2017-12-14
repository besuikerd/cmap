package com.besuikerd.cmap.integration.playjson

import com.besuikerd.cmap._
import com.besuikerd.cmap.integration.playjson
import play.api.libs.json._

trait CmapWithReads[T] extends Cmap[Context, Error, T] with Reads[T] {
  override def reads(json: Context): JsResult[T] = runCmap(json) match {
    case Left(value)  => JsError(value)
    case Right(value) => JsSuccess(value)
  }

  override def map[U](f: T => U): CmapWithReads[U] = new MappedCmapWithReads(this, f)
}

class CmapWithReadsWrapper[T](cmap: CmapReads[T]) extends CmapWithReads[T] { self =>
  override def runCmap(ctx: Context): Either[Error, T] = cmap.runCmap(ctx)
}

class MappedCmapWithReads[T, U](cmap: Cmap[Context, Error, T], f: T => U) extends CmapWithReads[U] {
  override def runCmap(ctx: playjson.Context): Either[Error, U] = new MappedCmap(cmap, f).runCmap(ctx)
}

class CmapForReads[T](implicit reads: Reads[T]) extends Cmap[Context, Error, T] {
  override def runCmap(ctx: playjson.Context): Either[playjson.Error, T] =
    reads.reads(ctx).asEither.left.map(_.map { case (p, seq) => (p, seq.toList) }.toList)
}

class CmapForPath[T](path: JsPath)(implicit reads: CmapReads[T]) extends Cmap[Context, Error, T] {
  override def runCmap(ctx: playjson.Context): Either[playjson.Error, T] = {
    val result = path
      .asSingleJson(ctx)
      .toEither
      .left
      .map(e => List((path, List(e))))
      .right
      .flatMap(reads.runCmap)
    result
  }
}
object CmapReads extends FixedCmapOps[Context, Error] {
  def fromPath[T](path: JsPath)(implicit cmap: CmapReads[T]): CmapReads[T] = new CmapForPath(path)(cmap)
  def fromReads[T](implicit reads: Reads[T]): CmapReads[T]                 = new CmapForReads[T]()(reads)

  object Implicits {
    implicit def cmapForPath[T](path: JsPath)(implicit cmap: CmapReads[T]): CmapReads[T] = fromPath(path)
    implicit def cmapForReads[T](implicit reads: Reads[T]): CmapReads[T]                 = fromReads
  }
}
