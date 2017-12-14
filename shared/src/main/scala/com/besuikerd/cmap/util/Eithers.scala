package com.besuikerd.cmap.util

object Eithers {
  def splitEithers[L, R](seq: Seq[Either[L, R]]): (Seq[L], Seq[R]) =
    seq.foldLeft((Seq.empty[L]), (Seq.empty[R])) {
      case ((ls, rs), Right(r)) => (ls, rs :+ r)
      case ((ls, rs), Left(l))  => (ls :+ l, rs)
    }
}
