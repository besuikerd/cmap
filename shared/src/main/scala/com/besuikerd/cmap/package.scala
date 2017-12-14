package com.besuikerd

package object cmap {
  type Partial3[T[_, _, _], A, B] = {
    type Apply[C] = T[A, B, C]
  }

  type FixCmap[Context, Error] = Partial3[Cmap, Context, Error]
}
