package com.besuikerd.cmap

package object typeclass {

  type FAlgebra[F[_], A] = F[A] => A

  type Id[F] = F
  type Const[T] = {
    type $[_]     = T
    type Apply[_] = T
  }

  val Fix: FixModule = FixImpl
  type Fix[F[_]] = Fix.Fix[F]
}
