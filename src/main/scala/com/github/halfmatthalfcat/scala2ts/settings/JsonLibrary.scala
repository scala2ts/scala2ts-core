package com.github.halfmatthalfcat.scala2ts.settings

import enumeratum.EnumEntry.Lowercase
import enumeratum._

sealed trait JsonLibrary extends EnumEntry with Lowercase

object JsonLibrary extends Enum[JsonLibrary] {
  val values = findValues

  case object Jackson1  extends JsonLibrary
  case object Jackson2  extends JsonLibrary
  case object Jaxb      extends JsonLibrary
  case object Gson      extends JsonLibrary
  case object Jsonb     extends JsonLibrary
}
