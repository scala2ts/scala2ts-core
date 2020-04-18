package com.github.scala2ts.configuration

import scala.util.matching.Regex

case class IncludeExclude(
  include: Seq[Regex] = Seq(),
  exclude: Seq[Regex] = Seq()
)