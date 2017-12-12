package com.besuikerd.cmap.integration.playjson

import com.besuikerd.cmap.integration.playjson
import com.besuikerd.cmap.{Cmap, CmapOps}
import play.api.libs.json._

class CmapForReads[T](implicit reads: Reads[T]) extends Cmap[playjson.Context, playjson.Error, T] {
  override def runCmap(ctx: playjson.Context): Either[playjson.Error, T] = reads.reads(ctx).asEither
}

class CmapForPath[T](path: JsPath)(implicit reads: CmapReads[T]) extends Cmap[playjson.Context, playjson.Error, T] {
  override def runCmap(ctx: playjson.Context) = {
    CmapReads
      .result[JsValue](
        path
          .asSingleJson(ctx)
          .toEither
          .left
          .map(e => Seq((path, Seq(e)))))
      .mapResult(reads.runCmap)
      .runCmap(ctx)
  }
}

object CmapReads extends CmapOps[playjson.Context, playjson.Error] {
  implicit def cmapforReads[T](implicit reads: Reads[T]): CmapReads[T]                  = new CmapForReads[T]
  implicit def cmapForPath[T](path: JsPath)(implicit reads: CmapReads[T]): CmapReads[T] = new CmapForPath(path)
}
