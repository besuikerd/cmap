package com.besuikerd.cmap.util

import scala.collection.generic.CanBuildFrom

object Eithers {
  def splitEithers[L, R, Coll[X] <: Traversable[X]](seq: Coll[Either[L, R]])(
      implicit cbfL: CanBuildFrom[Coll[R], L, Coll[L]],
      cbfR: CanBuildFrom[Coll[R], R, Coll[R]]
  ): (Coll[L], Coll[R]) = {
    val errors = cbfL()
    val values = cbfR()
    seq.foreach {
      case Left(l)  => errors += l
      case Right(r) => values += r
    }
    (errors.result(), values.result())
  }
}
