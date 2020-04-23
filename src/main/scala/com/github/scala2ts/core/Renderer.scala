package com.github.scala2ts.core

import com.github.scala2ts.configuration.Configuration
import com.github.scala2ts.model.Typescript.Declaration
import org.fusesource.scalate.{TemplateEngine, TemplateException}
import org.fusesource.scalate.util.{FileResourceLoader, Resource}

import scala.collection.immutable.ListSet

class Renderer(
  config: Configuration,
  engine: TemplateEngine
) {

  def render(tsTypes: ListSet[Declaration]): String = {
    try {
      engine.layout(
        "index.ssp", Map(
          "config" -> config,
          "types" -> tsTypes
        )
      )
    } catch {
      case ex: TemplateException =>
        System.out.println(String.format("TemplateError: %s\n%s", ex.getMessage, ex.getStackTrace.mkString("\n")))
        throw ex
    }
  }

}

object Renderer {
  def apply(config: Configuration): Renderer = {
    val engine: TemplateEngine = new TemplateEngine
    engine.allowCaching = false
    engine.allowReload = false
    engine.resourceLoader = new FileResourceLoader {
      override def resource(template: String): Option[Resource] = {
        Some(Resource.fromURL(
          this.getClass.getClassLoader.getResource(s"template/$template")
        ))
      }
    }

    new Renderer(config, engine)
  }
}