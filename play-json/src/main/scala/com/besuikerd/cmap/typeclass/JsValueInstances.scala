package com.besuikerd.cmap.typeclass

import com.besuikerd.cmap.integration.playjson
import play.api.libs.json._

object JsValueInstances {
  implicit object selfFoldableJsValue extends SelfFoldableJsValue
}

class SelfFoldableJsValue extends SelfFoldable[playjson.Context, playjson.Error] {
  override def foldLeft[U](t: JsValue, u: U)(f: (U, JsValue) => U): Either[playjson.Error, U] =
    t.validate[JsArray]
      .asEither
      .left
      .map(_.map { case (path, errors) => (path, errors.toList) }.toList)
      .right
      .map(_.value.foldLeft(u)(f))
}
