package com.github.scala2ts.configuration

object SealedTypesMapping extends Enumeration {
  type SealedTypesMapping = Value
  val AsEnum, AsUnion, AsUnionString, None = Value
}
