package com.github.halfmatthalfcat.scala2ts.settings

import enumeratum.EnumEntry.Camelcase
import enumeratum._

sealed trait NullabilityDefinition extends EnumEntry with Camelcase

object NullabilityDefinition extends Enum[NullabilityDefinition] {
  val values = findValues

  case object NullAndUndefinedUnion       extends NullabilityDefinition
  case object NullUnion                   extends NullabilityDefinition
  case object UndefinedUnion              extends NullabilityDefinition
  case object NullAndUndefinedInlineUnion extends NullabilityDefinition
  case object NullInlineUnion             extends NullabilityDefinition
  case object UndefinedInlineUnion        extends NullabilityDefinition
}
