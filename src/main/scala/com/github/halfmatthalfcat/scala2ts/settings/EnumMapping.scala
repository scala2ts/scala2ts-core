package com.github.halfmatthalfcat.scala2ts.settings

import enumeratum.EnumEntry.Camelcase
import enumeratum._

sealed trait EnumMapping extends EnumEntry with Camelcase

object EnumMapping extends Enum[EnumMapping] {
  val values = findValues

  case object AsUnion           extends EnumMapping
  case object AsInlineUnion     extends EnumMapping
  case object AsEnum            extends EnumMapping
  case object AsNumberBasedEnum extends EnumMapping
}
