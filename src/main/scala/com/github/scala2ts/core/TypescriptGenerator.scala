package com.github.scala2ts.core

import com.github.scala2ts.configuration.Configuration

import scala.reflect.api.Universe

object TypescriptGenerator {

  def generate(universe: Universe)(
    config: Configuration,
    types: List[universe.Type]
  ): Unit = {
    val parser = new ScalaParser[universe.type](universe)
    val transpiler = new Transpiler(config)

    val parsedTypes = parser.parseTypes(types)
    val typescript = transpiler(parsedTypes)

    System.out.println(String.format(
      "FOUND TYPES: \n%s\nTYPESCRIPT: \n%s\n",
      parsedTypes.mkString("\n"),
      typescript.mkString("\n")
    ))
  }

}
