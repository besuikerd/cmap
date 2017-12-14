package com.besuikerd.cmap.integration

import com.besuikerd.cmap.Cmap
import play.api.libs.json.{JsPath, JsValue, JsonValidationError}

package object playjson {
  type Context      = JsValue
  type Error        = List[(JsPath, List[JsonValidationError])]
  type CmapReads[T] = Cmap[Context, Error, T]
}
