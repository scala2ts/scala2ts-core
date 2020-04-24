package com.github.scala2ts.configuration

object DateMapping extends Enumeration {
  type DateMapping = Value
  val AsDate, AsString, AsNumber = Value
}
