package com.besuikerd.cmap.typeclass

import cats.{Applicative, Monoid}
import com.besuikerd.cmap.{ApplicativeCmap, Cmap, FixCmap, SuccessCmap}

object CmapInstances {
  implicit def applicativeForCmap[Context, Error: Monoid]: Applicative[FixCmap[Context, Error]#Apply] =
    new CmapApplicative[Context, Error]()
}

class CmapApplicative[Context, Error](implicit monoid: Monoid[Error])
    extends Applicative[FixCmap[Context, Error]#Apply] {

  override def pure[A](x: A) = new SuccessCmap(x)

  override def ap[A, B](ff: Cmap[Context, Error, A => B])(fa: Cmap[Context, Error, A]) = new ApplicativeCmap(ff, fa)
}
