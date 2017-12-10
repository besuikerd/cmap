package com.besuikerd.cmap

import com.besuikerd.cmap.typeclass.Monoid

trait CmapApplicativeApplyBuilder[Context, Error] { this: CmapOps[Context, Error] =>

  def apply[P, T1, T2](p: T1 => T2 => P)(m1: Cmap[Context, Error, T1], m2: Cmap[Context, Error, T2])(
      implicit monoid: Monoid[Error]): Cmap[Context, Error, P] =
    success(p).ap(m1).ap(m2)

  def apply[T1, T2](m1: Cmap[Context, Error, T1], m2: Cmap[Context, Error, T2])(
      implicit monoid: Monoid[Error]): Cmap[Context, Error, (T1, T2)] =
    success((Tuple2[T1, T2] _).curried).ap(m1).ap(m2)

  def apply[T1, T2, T3](m1: Cmap[Context, Error, T1], m2: Cmap[Context, Error, T2], m3: Cmap[Context, Error, T3])(
      implicit monoid: Monoid[Error]): Cmap[Context, Error, (T1, T2, T3)] =
    success((Tuple3[T1, T2, T3] _).curried).ap(m1).ap(m2).ap(m3)

  def apply[T1, T2, T3, T4](
      m1: Cmap[Context, Error, T1],
      m2: Cmap[Context, Error, T2],
      m3: Cmap[Context, Error, T3],
      m4: Cmap[Context, Error, T4])(implicit monoid: Monoid[Error]): Cmap[Context, Error, (T1, T2, T3, T4)] =
    success((Tuple4[T1, T2, T3, T4] _).curried).ap(m1).ap(m2).ap(m3).ap(m4)

//  def apply[P, T1, T2](p: T1 => T2 => P)(m1: Cmap[Ctx, Err, T1], m2: Cmap[Ctx, Err, T2])(
//      implicit semigroup: Semigroup[Err]): Cmap[Ctx, Err, P] =
//    Cmap.success[Ctx, Err, T1 => T2 => P](p).ap(m1).ap(m2)

  //  def apply[P, T1, T2, T3](p: T1 => T2 => T3 => P)(m1: Mapping[T1], m2: Mapping[T2], m3: Mapping[T3]): Mapping[P] =
//    const(p).app(m1).app(m2).app(m3)
//
//  def apply[P, T1, T2, T3, T4](
//                                p: T1 => T2 => T3 => T4 => P)(m1: Mapping[T1], m2: Mapping[T2], m3: Mapping[T3], m4: Mapping[T4]): Mapping[P] =
//    const(p).app(m1).app(m2).app(m3).app(m4)
//
//  def apply[P, T1, T2, T3, T4, T5](p: T1 => T2 => T3 => T4 => T5 => P)(m1: Mapping[T1],
//                                                                       m2: Mapping[T2],
//                                                                       m3: Mapping[T3],
//                                                                       m4: Mapping[T4],
//                                                                       m5: Mapping[T5]): Mapping[P] =
//    const(p).app(m1).app(m2).app(m3).app(m4).app(m5)
//
//  def apply[P, T1, T2, T3, T4, T5, T6](p: T1 => T2 => T3 => T4 => T5 => T6 => P)(m1: Mapping[T1],
//                                                                                 m2: Mapping[T2],
//                                                                                 m3: Mapping[T3],
//                                                                                 m4: Mapping[T4],
//                                                                                 m5: Mapping[T5],
//                                                                                 m6: Mapping[T6]): Mapping[P] =
//    const(p).app(m1).app(m2).app(m3).app(m4).app(m5).app(m6)
//
//  def apply[P, T1, T2, T3, T4, T5, T6, T7](p: T1 => T2 => T3 => T4 => T5 => T6 => T7 => P)(
//    m1: Mapping[T1],
//    m2: Mapping[T2],
//    m3: Mapping[T3],
//    m4: Mapping[T4],
//    m5: Mapping[T5],
//    m6: Mapping[T6],
//    m7: Mapping[T7]): Mapping[P] =
//    const(p).app(m1).app(m2).app(m3).app(m4).app(m5).app(m6).app(m7)
//
//  //scalastyle:off
//  def apply[P, T1, T2, T3, T4, T5, T6, T7, T8](p: T1 => T2 => T3 => T4 => T5 => T6 => T7 => T8 => P)(
//    m1: Mapping[T1],
//    m2: Mapping[T2],
//    m3: Mapping[T3],
//    m4: Mapping[T4],
//    m5: Mapping[T5],
//    m6: Mapping[T6],
//    m7: Mapping[T7],
//    m8: Mapping[T8]): Mapping[P] =
//    const(p).app(m1).app(m2).app(m3).app(m4).app(m5).app(m6).app(m7).app(m8)
//
//  def apply[P, T1, T2, T3, T4, T5, T6, T7, T8, T9](p: T1 => T2 => T3 => T4 => T5 => T6 => T7 => T8 => T9 => P)(
//    m1: Mapping[T1],
//    m2: Mapping[T2],
//    m3: Mapping[T3],
//    m4: Mapping[T4],
//    m5: Mapping[T5],
//    m6: Mapping[T6],
//    m7: Mapping[T7],
//    m8: Mapping[T8],
//    m9: Mapping[T9]): Mapping[P] =
//    const(p).app(m1).app(m2).app(m3).app(m4).app(m5).app(m6).app(m7).app(m8).app(m9)
//
//  def apply[P, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10](
//                                                         p: T1 => T2 => T3 => T4 => T5 => T6 => T7 => T8 => T9 => T10 => P)(m1: Mapping[T1],
//                                                                                                                            m2: Mapping[T2],
//                                                                                                                            m3: Mapping[T3],
//                                                                                                                            m4: Mapping[T4],
//                                                                                                                            m5: Mapping[T5],
//                                                                                                                            m6: Mapping[T6],
//                                                                                                                            m7: Mapping[T7],
//                                                                                                                            m8: Mapping[T8],
//                                                                                                                            m9: Mapping[T9],
//                                                                                                                            m10: Mapping[T10]): Mapping[P] =
//    const(p).app(m1).app(m2).app(m3).app(m4).app(m5).app(m6).app(m7).app(m8).app(m9).app(m10)
}
