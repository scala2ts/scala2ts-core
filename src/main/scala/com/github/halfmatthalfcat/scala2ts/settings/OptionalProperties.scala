package com.github.halfmatthalfcat.scala2ts.settings

import enumeratum.EnumEntry.Camelcase
import enumeratum._

sealed trait OptionalProperties extends EnumEntry with Camelcase

object OptionalProperties extends Enum[OptionalProperties] {
  val values = findValues

  case object UseSpecifiedAnnotations extends OptionalProperties
  case object UseLibraryDefinition    extends OptionalProperties
  case object All                     extends OptionalProperties
}
