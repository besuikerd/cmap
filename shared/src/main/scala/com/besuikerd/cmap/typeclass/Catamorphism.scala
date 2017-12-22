package com.besuikerd.cmap.typeclass

import cats.Functor
import com.besuikerd.cmap.Cmap

trait Catamorphism[T, F[_]] extends Functor[F] {
  def makeCata(t: T): Fix[F]

  def cata[U](algebra: FAlgebra[F, U])(f: Fix[F]): U =
    algebra(map(Fix.unfix(f))(cata(algebra)))

  def apply[U](algebra: FAlgebra[F, U])(t: T): U =
    cata(algebra)(makeCata(t))
}

class IdCatamorphism[F[_]](implicit functor: Functor[F]) extends Catamorphism[Fix[F], F] {
  override def makeCata(t: Fix[F]): Fix[F]          = t
  override def map[A, B](fa: F[A])(f: A => B): F[B] = functor.map(fa)(f)
}

object Catamorphism {}
