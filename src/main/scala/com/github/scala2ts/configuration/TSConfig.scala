package com.github.scala2ts.configuration

import OptionPickler.{Writer => W, macroW}

case class TSCompilerOptions(
  target: String,
  module: String,
  lib: Seq[String]
)

object TSCompilerOptions {
  implicit val rw: W[TSCompilerOptions] = macroW
}

case class TSConfig(
  compilerOptions: TSCompilerOptions
)

object TSConfig {
  implicit val rw: W[TSConfig] = macroW
}
