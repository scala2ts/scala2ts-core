package com.github.halfmatthalfcat.scala2ts.settings

import enumeratum._

sealed trait LoggingLevel extends EnumEntry

object LoggingLevel extends Enum[LoggingLevel] {
  val values = findValues

  case object Debug   extends LoggingLevel
  case object Verbose extends LoggingLevel
  case object Info    extends LoggingLevel
  case object Warning extends LoggingLevel
  case object Error   extends LoggingLevel
}
