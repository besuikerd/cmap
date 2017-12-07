package com.besuikerd.cmap

class ApplyCurriedBuilder[Ctx, Err] extends AnyRef {

//  def apply[P, T1, T2](p: T1 => T2 => P)(m1: Parser[Ctx, Err, T1], m2: Parser[Ctx, Err, T2]): Parser[Ctx, Err, P] =
//    StaticParser.success[Ctx, Err, T1 => T2 => P](p).ap(m1).ap(m2)

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
