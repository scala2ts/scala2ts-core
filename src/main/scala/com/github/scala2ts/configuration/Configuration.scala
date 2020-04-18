package com.github.scala2ts.configuration

case class Configuration(
  files: IncludeExclude = IncludeExclude(),
  types: IncludeExclude = IncludeExclude(),
)
