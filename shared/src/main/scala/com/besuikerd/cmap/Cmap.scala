package com.besuikerd.cmap

import cats._
import com.besuikerd.cmap.typeclass.SelfFoldable
import com.besuikerd.cmap.util.Eithers

trait Cmap[Context, Error, T] {

  def apply(ctx: Context): Either[Error, T] = runCmap(ctx)

  def runCmap(ctx: Context): Either[Error, T]

  def flatMap[U](f: T => Cmap[Context, Error, U]): Cmap[Context, Error, U] =
    new FlatMappedCmap(this, f)

  def map[U](f: T => U): Cmap[Context, Error, U] =
    new MappedCmap(this, f)

  def mapResult[U](f: T => Either[Error, U]): Cmap[Context, Error, U] =
    new MappedResultCmap(this, f)

  def optional: Cmap[Context, Error, Option[T]] = new OptionCmap(this)
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

class OptionCmap[Context, Error, T](cmap: Cmap[Context, Error, T]) extends Cmap[Context, Error, Option[T]] {
  override def runCmap(ctx: Context) = {
    val inner = cmap.runCmap(ctx)
    val res   = Right(inner.toOption)
    res
  }
}

class ApplicativeCmap[Context, Error, T, U](mf: Cmap[Context, Error, T => U], mt: Cmap[Context, Error, T])(
    implicit monoid: Monoid[Error])
    extends Cmap[Context, Error, U] {
  override def runCmap(ctx: Context): Either[Error, U] = (mf.runCmap(ctx), mt.runCmap(ctx)) match {
    case (Right(ft), Right(t))   => Right(ft(t))
    case (Left(err), Left(err2)) => Left(monoid.combine(err, err2))
    case (Left(err), _)          => Left(err)
    case (_, Left(err))          => Left(err)
  }
}

class CmapList[Context, Error, T](cmap: Cmap[Context, Error, T])(implicit foldCtx: SelfFoldable[Context, Error],
                                                                 monoidErr: Monoid[Error])
    extends Cmap[Context, Error, List[T]] {
  override def runCmap(ctx: Context): Either[Error, List[T]] = {
    foldCtx
      .foldLeft[List[Either[Error, T]]](ctx, List.empty) {
        case (acc, cur) => acc :+ cmap.runCmap(cur)
      }
      .right
      .flatMap { folded =>
        val (errors, results) = Eithers.splitEithers(folded)
        if (errors.isEmpty) {
          Right(results.toList)
        } else {
          Left(monoidErr.combineAll(errors))
        }
      }
  }
}

private class LazyCmap[Context, Error, T](fn: => Cmap[Context, Error, T]) extends Cmap[Context, Error, T] {
  lazy val materialized: Cmap[Context, Error, T]       = fn
  override def runCmap(ctx: Context): Either[Error, T] = materialized.runCmap(ctx)
}

private class LazyLetCmap[Context, Error, T](var materialized: Cmap[Context, Error, T])
    extends Cmap[Context, Error, T] {
  override def runCmap(ctx: Context): Either[Error, T] = materialized.runCmap(ctx)
}

class FixedCmapOps[Context, Error](implicit ctx: ContextType[Context], error: ErrorType[Error])
    extends AnyRef
    with CmapApplicativeApplyBuilder[Context, Error] {

  implicit def contextType: ContextType[Context] = ctx
  implicit def errorType: ErrorType[Error]       = error

  def apply[T](cmap: Context => Either[Error, T]): Cmap[Context, Error, T] = new ApplyCmap(cmap)
  def success[T](value: T): Cmap[Context, Error, T]                        = new SuccessCmap(value)
  def fail[T](error: Error): Cmap[Context, Error, T]                       = new FailCmap(error)
  def result[T](result: Either[Error, T]): Cmap[Context, Error, T]         = new ResultCmap(result)

  def sequence[T](cmap: Cmap[Context, Error, T])(implicit foldCtx: SelfFoldable[Context, Error],
                                                 monoidErr: Monoid[Error]): Cmap[Context, Error, List[T]] =
    new CmapList(cmap)

  def lazily[T](fn: => Cmap[Context, Error, T]): Cmap[Context, Error, T] =
    new LazyCmap(fn)

  def lazilyLet[T](fn: Cmap[Context, Error, T] => Cmap[Context, Error, T]): Cmap[Context, Error, T] = {
    val placeholder = new LazyLetCmap[Context, Error, T](materialized = null)
    val cmap        = fn(placeholder)
    placeholder.materialized = cmap
    placeholder
  }
}

trait ContextType[T] {}
object ContextType {
  implicit def contextType[T]: ContextType[T] = new ContextType[T] {}
}

trait ErrorType[T] {}
object ErrorType {
  implicit def errorType[T]: ErrorType[T] = new ErrorType[T] {}
}

object Cmap {

  import cats._

  implicit class ApplicativeCmapOps[Context, Error, T, U](val mf: Cmap[Context, Error, T => U]) extends AnyVal {
    def ap(mt: Cmap[Context, Error, T])(
        implicit ap: Applicative[Partial3[Cmap, Context, Error]#Apply]): Cmap[Context, Error, U] = {
      ap.ap(mf)(mt)
    }

  }
}
