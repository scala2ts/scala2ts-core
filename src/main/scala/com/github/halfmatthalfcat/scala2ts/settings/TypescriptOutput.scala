package com.github.halfmatthalfcat.scala2ts.settings

import enumeratum.EnumEntry.Camelcase
import enumeratum._

sealed trait TypescriptOutput extends EnumEntry with Camelcase

object TypescriptOutput extends Enum[TypescriptOutput] {
  val values = findValues

  case object Global        extends TypescriptOutput
  case object Module        extends TypescriptOutput
  case object AmbientModule extends TypescriptOutput
}
