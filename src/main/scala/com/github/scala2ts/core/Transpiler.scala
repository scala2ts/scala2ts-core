package com.github.scala2ts.core

import com.github.scala2ts.configuration.{Configuration, DateMapping, LongDoubleMapping}
import com.github.scala2ts.model.Typescript._
import com.github.scala2ts.model.{Scala, Typescript}

import scala.collection.immutable.ListSet

final class Transpiler(config: Configuration) {
  @inline def apply(scalaTypes: ListSet[Scala.TypeDef]): ListSet[Declaration] =
    apply(scalaTypes, superInterface = None)

  def apply(
    scalaTypes: ListSet[Scala.TypeDef],
    superInterface: Option[InterfaceDeclaration]
  ): ListSet[Declaration] =
    // TODO: sealed traits and objects?
    scalaTypes.collect {
      case scalaClass: Scala.CaseClass =>
        transpileInterface(scalaClass, superInterface)
    }

  private def transpileInterface(
    scalaClass: Scala.CaseClass,
    superInterface: Option[InterfaceDeclaration]
  ) = InterfaceDeclaration(
    buildInterfaceName(scalaClass.name),
    scalaClass.fields.map { scalaMember =>
      Typescript.Member(
        scalaMember.name,
        transpileTypeRef(scalaMember.typeRef, inInterfaceContext = true))
    },
    typeParams = scalaClass.typeArgs,
    superInterface = superInterface)

  private def buildInterfaceName(name: String) =
    s"${config.typeNamePrefix}${name}${config.typeNameSuffix}"

  private def transpileTypeRef(
    scalaTypeRef: Scala.TypeRef,
    inInterfaceContext: Boolean
  ): Typescript.TypeRef = scalaTypeRef match {
    case Scala.IntRef =>
      Typescript.NumberRef

    case Scala.LongRef | Scala.DoubleRef =>
      if (config.longDoubleMapping == LongDoubleMapping.AsNumber) {
        Typescript.NumberRef
      } else {
        Typescript.StringRef
      }

    case Scala.BooleanRef =>
      Typescript.BooleanRef

    case Scala.StringRef =>
      Typescript.StringRef

    case Scala.SeqRef(innerType) =>
      Typescript.ArrayRef(transpileTypeRef(innerType, inInterfaceContext))

    case Scala.CaseClassRef(name, typeArgs) =>
      val actualName = if (inInterfaceContext) buildInterfaceName(name) else name
      Typescript.CustomTypeRef(
        actualName, typeArgs.map(transpileTypeRef(_, inInterfaceContext)))

    case Scala.DateRef =>
      if (config.dateMapping == DateMapping.AsNumber) {
        Typescript.NumberRef
      } else if (config.dateMapping == DateMapping.AsString) {
        Typescript.StringRef
      } else {
        Typescript.DateRef
      }

    case Scala.TypeParamRef(name) =>
      Typescript.SimpleTypeRef(name)

    case Scala.OptionRef(innerType) =>
      Typescript.OptionRef(transpileTypeRef(innerType, inInterfaceContext))

    case Scala.MapRef(kT, vT) => Typescript.MapType(
      transpileTypeRef(kT, inInterfaceContext),
      transpileTypeRef(vT, inInterfaceContext))

    case Scala.UnionRef(possibilities) =>
      Typescript.UnionType(possibilities.map { i =>
        transpileTypeRef(i, inInterfaceContext)
      })

    case Scala.UnknownTypeRef(_) =>
      Typescript.StringRef
  }
}
