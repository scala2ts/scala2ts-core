package com.github.halfmatthalfcat.scala2ts.settings

import enumeratum.EnumEntry.Camelcase
import enumeratum._

sealed trait DateMapping extends EnumEntry with Camelcase

object DateMapping extends Enum[DateMapping] {
  val values = findValues

  case object AsDate    extends DateMapping
  case object AsNumber  extends DateMapping
  case object AsString  extends DateMapping
}
