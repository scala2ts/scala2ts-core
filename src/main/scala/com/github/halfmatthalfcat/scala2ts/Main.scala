package com.github.halfmatthalfcat.scala2ts

import cz.habarta.typescript.generator.TypeScriptGenerator

object Main {
  def main(args: Array[String]): Unit = {
    val generator: TypeScriptGenerator =
      new TypeScriptGenerator();

    generator.generateTypeScript()
  }
}
