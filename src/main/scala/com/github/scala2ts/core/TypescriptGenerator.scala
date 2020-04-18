package com.github.scala2ts.core

import com.github.scala2ts.configuration.Configuration

import scala.reflect.api.Universe

object TypescriptGenerator {

  def generate(universe: Universe)(
    config: Configuration,
    types: List[universe.Type],
  ): Unit = {
    val parser = new ScalaParser[universe.type](universe)
    val parsedTypes = parser.parseTypes(types)

    System.out.println(String.format("PARSED TYPES:\n%s",
      parsedTypes.mkString("\n")
    ))
  }

}
