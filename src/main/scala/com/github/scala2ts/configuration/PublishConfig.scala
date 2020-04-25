package com.github.scala2ts.configuration

import OptionPickler.{Writer => W, macroW}

case class PublishConfig(
  registry: String
)

object PublishConfig {
  implicit val rw: W[PublishConfig] = macroW
}