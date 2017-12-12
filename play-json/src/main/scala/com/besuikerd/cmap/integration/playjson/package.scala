package com.besuikerd.cmap.integration

import com.besuikerd.cmap.Cmap
import play.api.libs.json.{JsPath, JsValue, JsonValidationError}

package object playjson {
  type Context      = JsValue
  type Error        = Seq[(JsPath, Seq[JsonValidationError])]
  type CmapReads[T] = Cmap[Context, Error, T]
}
