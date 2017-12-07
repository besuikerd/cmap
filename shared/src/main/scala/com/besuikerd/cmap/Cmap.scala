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

class CmapOps[Context, Error] extends AnyRef {
  def apply[T](cmap: Context => Either[Error, T]): Cmap[Context, Error, T] = new ApplyCmap(cmap)
  def success[T](value: T): Cmap[Context, Error, T]                        = new SuccessCmap(value)
  def fail[T](error: Error): Cmap[Context, Error, T]                       = new FailCmap(error)
}

object Cmap {
  def combinators[Context, Error] = new CmapOps[Context, Error]

  implicit class ApplicativeCmapOps[Context, Error, T, U](val mf: Cmap[Context, Error, T => U]) extends AnyVal {
    def ap(mt: Cmap[Context, Error, T])(implicit monoid: Monoid[Error]): Cmap[Context, Error, U] =
      new ApplicativeCmap(mf, mt)
  }
}
//class ApplyCtxMapping[Ctx, Err, T](val ctxMapping: Parser[Ctx, Err, T]) extends AnyVal {
//  def apply(ctx: Ctx): Either[Err, (Ctx, T)] = ctxMapping.runParser(ctx)
//}
//
//class ApplyStaticCtxMapping[Ctx, Err, T](val ctxMapping: Cmap[Ctx, Err, T]) extends AnyVal {
//  def apply(ctx: Ctx): Either[Err, T] = ctxMapping.runStaticParser(ctx)
//}
//
//object Parser {
//  def apply[Ctx, Err, T](f: Ctx => Either[Err, (Ctx, T)]): Parser[Ctx, Err, T] = new ApplyParser(f)
//
//  implicit class ApplicativeParser[Ctx, Err, T, U](val pf: Parser[Ctx, Err, T => U]) extends AnyVal {
//    def ap(p: Parser[Ctx, Err, T]): Parser[Ctx, Err, U] =
//      Parser(ctx =>
//        p.runParser(ctx) match {
//          case Left(err) => Left(err)
//          case Right((ctx2, t)) =>
//            pf.runParser(ctx2) match {
//              case Left(err)          => Left(err)
//              case Right((ctx3, ftu)) => Right((ctx3, ftu(t)))
//            }
//      })
//  }
//}
//
//object Cmap {
//  def apply[Ctx, Err, T](f: Ctx => Either[Err, T]): Cmap[Ctx, Err, T] = new ApplyStaticParser(f)
//
//  def success[Ctx, Err, T](value: T): Cmap[Ctx, Err, T] = Cmap.apply(_ => Right(value))
//  def fail[Ctx, Err, T](error: Err): Cmap[Ctx, Err, T]  = Cmap.apply(_ => Left(error))
//
//  implicit class ApplicativeParser[Ctx, Err, T, U](val pf: Cmap[Ctx, Err, T => U])(implicit semigroup: Semigroup[Err]) {
//    def ap(p: Cmap[Ctx, Err, T]): Cmap[Ctx, Err, U] =
//      Cmap(ctx =>
//        (p.runStaticParser(ctx), pf.runStaticParser(ctx)) match {
//          case (Right(t), Right(ft))   => Right(ft(t))
//          case (Left(err), Left(err2)) => Left(semigroup.append(err, err2))
//          case (Left(err), _)          => Left(err)
//          case (_, Left(err))          => Left(err)
//      })
//  }
//}
//
//class ApplyParser[Ctx, Err, T](f: Ctx => Either[Err, (Ctx, T)]) extends Parser[Ctx, Err, T] {
//  override def runParser(ctx: Ctx) = f(ctx)
//}
//
//class ApplyStaticParser[Ctx, Err, T](f: Ctx => Either[Err, T]) extends Cmap[Ctx, Err, T] {
//  override def runStaticParser(ctx: Context) = f(ctx)
//}
//
//object Mapping {
//  type Ctx        = (String => Either[String, String])
//  type Err        = List[String]
//  type Mapping[T] = Cmap[Ctx, Err, T]
//
//  def apply = new ApplyCurriedBuilder[Ctx, Err]
//
//  def mkCtx(values: (String, String)*): Ctx =
//    columnName => values.toMap.get(columnName).map(Right(_)).getOrElse(Left("Column does not exist"))
//
//  def mapString(value: String): Mapping[String] = Cmap(lookup => lookup(value).left.map(List(_)))
//  def mapInt(value: String): Mapping[Int] =
//    mapString(value).mapResult(s =>
//      try {
//        Right(s.toInt)
//      } catch {
//        case e: NumberFormatException => Left(List(s"Could not convert $s to Int"))
//    })
//
//  def success[T](value: T): Mapping[T] = Cmap.success(value)
//}
