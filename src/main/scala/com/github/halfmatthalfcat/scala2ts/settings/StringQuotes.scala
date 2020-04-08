package com.github.halfmatthalfcat.scala2ts.settings

import enumeratum._
import enumeratum.EnumEntry.Camelcase

sealed trait StringQuotes extends EnumEntry with Camelcase

object StringQuotes extends Enum[StringQuotes] {
  val values = findValues

  case object DoubleQuotes extends StringQuotes
  case object SingleQuotes extends StringQuotes
}
