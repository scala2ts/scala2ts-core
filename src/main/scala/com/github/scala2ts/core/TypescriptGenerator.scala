package com.github.scala2ts.core

import com.github.scala2ts.configuration.Configuration
import com.github.scala2ts.model.Scala.TypeDef
import com.github.scala2ts.model.Typescript.Declaration

import scala.collection.immutable.ListSet
import scala.reflect.api.Universe

object TypescriptGenerator {

  def generate(universe: Universe)(
    config: Configuration,
    types: List[universe.Type]
  ): Unit = {
    val parser: ScalaParser[universe.type] =
      new ScalaParser[universe.type](universe)
    val transpiler: Transpiler = new Transpiler(config)

    val parsedTypes: ListSet[TypeDef] = parser.parseTypes(types)
    val tsTypes: ListSet[Declaration] = transpiler(parsedTypes)

    val renderer: Renderer = Renderer(config)
    val output: String = renderer.render(tsTypes)

    System.out.println(output)
  }

}
