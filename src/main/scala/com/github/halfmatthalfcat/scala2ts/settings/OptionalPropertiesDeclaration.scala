package com.github.halfmatthalfcat.scala2ts.settings

import enumeratum.EnumEntry.Camelcase
import enumeratum._

sealed trait OptionalPropertiesDeclaration extends EnumEntry with Camelcase

object OptionalPropertiesDeclaration extends Enum[OptionalPropertiesDeclaration] {
  val values = findValues

  case object QuestionMark                extends OptionalPropertiesDeclaration
  case object QuestionMarkAndNullableType extends OptionalPropertiesDeclaration
  case object NullableType                extends OptionalPropertiesDeclaration
  case object NullableAndUndefinableType  extends OptionalPropertiesDeclaration
  case object UndefinableType             extends OptionalPropertiesDeclaration
}
