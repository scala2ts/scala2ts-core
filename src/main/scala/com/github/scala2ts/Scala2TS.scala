package com.github.scala2ts

import java.io.File

import com.github.scala2ts.configuration.Configuration
import com.github.scala2ts.core.TypescriptGenerator

import scala.tools.nsc
import nsc.Global
import nsc.Phase
import nsc.plugins.Plugin
import nsc.plugins.PluginComponent
import scala.util.matching.Regex

class Scala2TS(val global: Global) extends Plugin { plugin =>

  val name: String = "scala2ts"
  val description: String = "Create Typescript Definitions from Scala"
  val components: List[PluginComponent] = List(Component)

  private var config: Configuration = Configuration()

  override def processOptions(
    options: List[String],
    error: String => Unit
  ): Unit = { config = config.fromCompilerOptions(options) }

  private object Component extends PluginComponent {
    val global: Scala2TS.this.global.type = Scala2TS.this.global
    val runsAfter: List[String] = List[String]("typer")
    val phaseName: String = Scala2TS.this.name

    override def newPhase(prev: Phase): Phase = new TypescriptPhase(prev)

    private final class TypescriptPhase(prev: Phase) extends StdPhase(prev) {
      import global._

      override def name: String = Scala2TS.this.name

      override def apply(unit: CompilationUnit): Unit = {
        val file: File = unit.source.file.file

        if (includesFile(file)) {
          if (config.debug) {
            global.inform(s"${plugin.name}.debug: Checking file ${file.getAbsoluteFile}")
          }

          handle(unit)
        } else {
          if (config.debug) {
            global.inform(s"${plugin.name}.debug: Skipping file ${file.getAbsoluteFile}")
          }
        }
      }

      /**
       * Check whether the current file is included via the Configuration
       */
      private def includesFile(file: File): Boolean = {
        val path: String = file.getAbsolutePath

        if (!matches(path, config.files.include)) {
          false
        } else {
          !matches(path, config.files.exclude)
        }
      }

      /**
       * Check whether the current symbol is included via the Configuration
       */
      private def includesType(sym: Symbol): Boolean = {
        val symName: String = {
          if (sym.isModule) s"object:${sym.fullName}"
          else s"class:${sym.fullName}"
        }

        if (config.types.include.isEmpty && config.types.exclude.isEmpty) {
          true
        } else if (!matches(symName, config.types.include)) {
          false
        } else {
          !matches(symName, config.types.exclude)
        }
      }

      @annotation.tailrec
      private def matches(str: String, regexs: Seq[Regex]): Boolean = {
        regexs.headOption match {
          case Some(re) => re.findFirstIn(str) match {
            case Some(_) => true
            case _ => matches(str, regexs.tail)
          }
          case _ => false
        }
      }

      private def handle(unit: CompilationUnit): Unit = {
        val types: List[Type] =  unit.body.children.flatMap { tree: Tree =>
          val sym: Symbol = tree.symbol

          if (sym.isModule && !sym.hasPackageFlag) {
            if (includesType(sym)) {
              if (config.debug) {
                global.inform(s"${plugin.name}.debug: Handling object ${sym.fullName}")
              }

              Seq(sym.typeSignature)
            } else {
              if (config.debug) {
                global.inform(s"${plugin.name}.debug: Skipping object ${sym.fullName}")
              }

              Seq.empty
            }
          } else if (sym.isClass) {
            if (includesType(sym)) {
              if (config.debug) {
                global.inform(s"${plugin.name}.debug: Handling class ${sym.fullName}")
              }

              Seq(sym.typeSignature)
            } else {
              if (config.debug) {
                global.inform(s"${plugin.name}.debug: Skipping class ${sym.fullName}")
              }

              Seq.empty
            }
          } else {
            Seq.empty
          }
        }

        TypescriptGenerator.generate(global)(
          config = plugin.config,
          types = types
        )
      }
    }
  }

}
