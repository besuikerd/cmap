package com.besuikerd.cmap.typeclass

import cats.Functor
import play.api.libs.json._

sealed trait JsValueF[+F]
case class JsNullF[F]()                         extends JsValueF[F]
case class JsBooleanF[F](value: Boolean)        extends JsValueF[F]
case class JsNumberF[F](value: BigDecimal)      extends JsValueF[F]
case class JsStringF[F](value: String)          extends JsValueF[F]
case class JsArrayF[F](values: IndexedSeq[F])   extends JsValueF[F]
case class JsObjectF[F](fields: Map[String, F]) extends JsValueF[F]

object JsValueF {
  val nil: Fix[JsValueF]                       = Fix.apply[JsValueF](JsNullF())
  def boolean(value: Boolean): Fix[JsValueF]   = Fix.apply[JsValueF](JsBooleanF(value))
  def number(value: BigDecimal): Fix[JsValueF] = Fix.apply[JsValueF](JsNumberF(value))
  def string(value: String): Fix[JsValueF]     = Fix.apply[JsValueF](JsStringF(value))
  def array(values: IndexedSeq[Fix[JsValueF]]): Fix[JsValueF] =
    Fix(JsArrayF(values))
  def obj(fields: Map[String, Fix[JsValueF]]): Fix[JsValueF] = Fix(JsObjectF(fields))

  def fromJsValue(json: JsValue): Fix[JsValueF] = json match {
    case JsNull               => nil
    case JsTrue               => boolean(true)
    case JsFalse              => boolean(false)
    case JsNumber(value)      => number(value)
    case JsString(value)      => string(value)
    case JsArray(value)       => array(value.map(fromJsValue))
    case JsObject(underlying) => obj(underlying.mapValues(fromJsValue).toMap)
  }

  def toJsValue: FAlgebra[JsValueF, JsValue] = {
    case JsNullF()         => JsNull
    case JsBooleanF(value) => if (value) JsTrue else JsFalse
    case JsNumberF(value)  => JsNumber(value)
    case JsStringF(value)  => JsString(value)
    case JsArrayF(values)  => JsArray(values)
    case JsObjectF(fields) => JsObject(fields)
  }
}

trait JsValueFFunctor extends Functor[JsValueF] {
  override def map[A, B](fa: JsValueF[A])(f: A => B): JsValueF[B] = fa match {
    case JsNullF()         => JsNullF()
    case JsBooleanF(value) => JsBooleanF(value)
    case JsNumberF(value)  => JsNumberF(value)
    case JsStringF(value)  => JsStringF(value)
    case JsArrayF(values)  => JsArrayF(values.map(f))
    case JsObjectF(fields) => JsObjectF(fields.mapValues(f))
  }
}

trait JsValueCatamorphism extends Catamorphism[JsValue, JsValueF] with JsValueFFunctor {
  implicit val catamorphism: Catamorphism[JsValue, JsValueF] = this
  override def makeCata(t: JsValue): Fix[JsValueF]           = JsValueF.fromJsValue(t)
}
