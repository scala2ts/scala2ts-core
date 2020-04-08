package com.github.halfmatthalfcat.scala2ts.settings

import enumeratum._
import enumeratum.EnumEntry.Camelcase

sealed trait RestNamespacing extends EnumEntry with Camelcase

object RestNamespacing extends Enum[RestNamespacing] {
  val values = findValues

  case object SingleObject  extends RestNamespacing
  case object PerResource   extends RestNamespacing
  case object ByAnnotation  extends RestNamespacing
}
