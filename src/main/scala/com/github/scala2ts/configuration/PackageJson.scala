package com.github.scala2ts.configuration

import OptionPickler.{Writer => W, macroW}

case class PackageJson(
  name: Option[String] = None,
  version: Option[String] = None,
  types: Option[String] = None,
  publishConfig: Option[PublishConfig] = None
)

object PackageJson {
  implicit val rw: W[PackageJson] = macroW
}