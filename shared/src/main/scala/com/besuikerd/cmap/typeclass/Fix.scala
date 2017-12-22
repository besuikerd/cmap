package com.besuikerd.cmap.typeclass

import scalaz.Scalaz._
import scalaz.syntax.all._
import com.besuikerd.cmap.typeclass
import shapeless._

import scalaz.Liskov

trait FixModule {
  type Fix[F[_]]

  def apply[F[_]](f: F[typeclass.Fix[F]]): Fix[F] = fix(f)

  def fix[F[_]](f: F[typeclass.Fix[F]]): Fix[F]
  def unfix[F[_]](f: Fix[F]): F[typeclass.Fix[F]]
}

private[typeclass] object FixImpl extends FixModule {
  type Fix[F[_]] = F[typeclass.Fix[F]]

  def fix[F[_]](f: F[typeclass.Fix[F]]): Fix[F]   = f
  def unfix[F[_]](f: Fix[F]): F[typeclass.Fix[F]] = f
}
