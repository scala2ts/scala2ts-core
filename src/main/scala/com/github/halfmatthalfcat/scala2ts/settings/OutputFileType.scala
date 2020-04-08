package com.github.halfmatthalfcat.scala2ts.settings

import enumeratum.EnumEntry.Camelcase
import enumeratum._

sealed trait OutputFileType extends EnumEntry with Camelcase

object OutputFileType extends Enum[OutputFileType] {
  val values = findValues

  case object DeclarationFile     extends OutputFileType
  case object ImplementationFile  extends OutputFileType
}
