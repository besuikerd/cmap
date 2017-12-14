package com.besuikerd.cmap.typeclass

trait SelfFoldable[T, Error] {
  def foldLeft[U](t: T, u: U)(f: (U, T) => U): Either[Error, U]
}
