package com.github.scala2ts.configuration

import OptionPickler.{Writer => W, macroW}

case class TSConfig(
  compilerOptions: Map[String, String]
)

object TSConfig {
  implicit val rw: W[TSConfig] = macroW
}
