package com.github.scala2ts.core

import com.github.scala2ts.configuration.{Configuration, OptionPickler, TSCompilerOptions, TSConfig}
import com.github.scala2ts.model.Scala.TypeDef
import com.github.scala2ts.model.Typescript.Declaration

import scala.collection.immutable.ListSet
import scala.reflect.api.Universe
import os.{/, Path}

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

    val output: String = Renderer(config, tsTypes)

    if (config.outDir.nonEmpty) {
      os.write.over(
        Path(config.outDir.get) / config.outFileName,
        output,
        createFolders = true
      )

      os.write.over(
        Path(config.outDir.get) / "tsconfig.json",
        OptionPickler.write(TSConfig(TSCompilerOptions(
          "es5",
          "commonjs",
          Seq("es6", "esnext")
        )), 2),
        createFolders = true
      )

      if (config.packageJson.name.nonEmpty) {
        os.write.over(
          Path(config.outDir.get) / "package.json",
          OptionPickler.write(config.packageJson, 2),
          createFolders = true
        )
      }
    }
  }

}
