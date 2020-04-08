package com.github.halfmatthalfcat.scala2ts.settings

import enumeratum.EnumEntry.Camelcase
import enumeratum._

sealed trait ClassMapping extends EnumEntry with Camelcase

object ClassMapping extends Enum[ClassMapping] {
  val values = findValues

  case object AsInterfaces  extends ClassMapping
  case object AsClasses     extends ClassMapping
}
