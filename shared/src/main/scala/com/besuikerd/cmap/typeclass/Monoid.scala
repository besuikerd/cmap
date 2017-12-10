package com.besuikerd.cmap.typeclass

trait Monoid[A] extends Any { self =>
  def empty: A
  def append(a1: A, a2: A): A
}

object Monoid {

  implicit object StringMonoid extends Monoid[String] {
    override def empty                                  = ""
    override def append(a1: String, a2: String): String = a1 ++ a2
  }

  implicit def listMonoid[T]: ListMonoid[T] = new ListMonoid[T]
  class ListMonoid[T](val unit: Unit) extends AnyVal with Monoid[List[T]] {
    override def empty                                     = List.empty
    override def append(a1: List[T], a2: List[T]): List[T] = a1 ++ a2
  }
}
