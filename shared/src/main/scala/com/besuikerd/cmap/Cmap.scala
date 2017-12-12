package com.besuikerd.cmap

import com.besuikerd.cmap.typeclass.Monoid

trait Cmap[Context, Error, T] {
  type Result = Either[Error, T]

  def apply(ctx: Context): Either[Error, T] = runCmap(ctx)

  def runCmap(ctx: Context): Either[Error, T]

  def flatMap[U](f: T => Cmap[Context, Error, U]): Cmap[Context, Error, U] =
    new FlatMappedCmap(this, f)

  def map[U](f: T => U): Cmap[Context, Error, U] =
    new MappedCmap(this, f)

  def mapResult[U](f: T => Either[Error, U]): Cmap[Context, Error, U] =
    new MappedResultCmap(this, f)
}

class FlatMappedCmap[Context, Error, T, U](cmap: Cmap[Context, Error, T], f: T => Cmap[Context, Error, U])
    extends Cmap[Context, Error, U] {
  override def runCmap(ctx: Context): Either[Error, U] =
    cmap.runCmap(ctx) match {
      case Left(e)  => Left(e)
      case Right(t) => f(t).runCmap(ctx)
    }
}

class MappedCmap[Context, Error, T, U](cmap: Cmap[Context, Error, T], f: T => U) extends Cmap[Context, Error, U] {
  override def runCmap(ctx: Context): Either[Error, U] = cmap.runCmap(ctx).right.map(f)
}

class MappedResultCmap[Context, Error, T, U](cmap: Cmap[Context, Error, T], f: T => Either[Error, U])
    extends Cmap[Context, Error, U] {
  override def runCmap(ctx: Context): Either[Error, U] = cmap.runCmap(ctx).right.flatMap(f)
}

class SuccessCmap[Context, Error, T](value: T) extends Cmap[Context, Error, T] {
  override def runCmap(ctx: Context) = Right(value)
}

class FailCmap[Context, Error, T](error: Error) extends Cmap[Context, Error, T] {
  override def runCmap(ctx: Context) = Left(error)
}

class ResultCmap[Context, Error, T](result: Either[Error, T]) extends Cmap[Context, Error, T] {
  override def runCmap(ctx: Context) = result
}

class ApplyCmap[Context, Error, T](cmap: Context => Either[Error, T]) extends Cmap[Context, Error, T] {
  override def runCmap(ctx: Context) = cmap.apply(ctx)
}

class ApplicativeCmap[Context, Error, T, U](mf: Cmap[Context, Error, T => U], mt: Cmap[Context, Error, T])(
    implicit monoid: Monoid[Error])
    extends Cmap[Context, Error, U] {
  override def runCmap(ctx: Context): Either[Error, U] = (mf.runCmap(ctx), mt.runCmap(ctx)) match {
    case (Right(ft), Right(t))   => Right(ft(t))
    case (Left(err), Left(err2)) => Left(monoid.append(err, err2))
    case (Left(err), _)          => Left(err)
    case (_, Left(err))          => Left(err)
  }
}

trait CmapOps[Context, Error] extends AnyRef with CmapApplicativeApplyBuilder[Context, Error] {

  def apply[T](cmap: Context => Either[Error, T]): Cmap[Context, Error, T] = new ApplyCmap(cmap)
  def success[T](value: T): Cmap[Context, Error, T]                        = new SuccessCmap(value)
  def fail[T](error: Error): Cmap[Context, Error, T]                       = new FailCmap(error)
  def result[T](result: Either[Error, T]): Cmap[Context, Error, T]         = new ResultCmap(result)
}

object Cmap {
  implicit class ApplicativeCmapOps[Context, Error, T, U](val mf: Cmap[Context, Error, T => U]) extends AnyVal {
    def ap(mt: Cmap[Context, Error, T])(implicit monoid: Monoid[Error]): Cmap[Context, Error, U] =
      new ApplicativeCmap(mf, mt)
  }
}
