package com.github.scala2ts.core

import com.github.scala2ts.configuration.Configuration
import com.github.scala2ts.model.Typescript._
import com.github.scala2ts.model.{Scala, Typescript}

import scala.collection.immutable.ListSet

final class Transpiler(config: Configuration) {
  @inline def apply(scalaTypes: ListSet[Scala.TypeDef]): ListSet[Declaration] =
    apply(scalaTypes, superInterface = None)

  def apply(
    scalaTypes: ListSet[Scala.TypeDef],
    superInterface: Option[InterfaceDeclaration]): ListSet[Declaration] =
    scalaTypes.flatMap { typeDef =>
      typeDef match {
        case scalaClass: Scala.CaseClass => {
          val clazz = {
            if (config.emitClasses) {
              ListSet[Declaration](transpileClass(scalaClass, superInterface))
            } else ListSet.empty[Declaration]
          }

          if (!config.emitInterfaces) clazz
          else ListSet[Declaration](
            transpileInterface(scalaClass, superInterface)) ++ clazz
        }

        case Scala.CaseObject(name, members) => {
          val values = members.map { scalaMember =>
            Member(
              scalaMember.name,
              transpileTypeRef(scalaMember.typeRef, inInterfaceContext = false))
          }

          ListSet[Declaration](
            SingletonDeclaration(name, values, superInterface))
        }

        case Scala.SealedUnion(name, fields, possibilities) => {
          val ifaceFields = fields.map { scalaMember =>
            Member(
              scalaMember.name,
              transpileTypeRef(scalaMember.typeRef, inInterfaceContext = false))
          }

          val unionRef = InterfaceDeclaration(
            s"I${name}", ifaceFields, ListSet.empty[String], superInterface)

          apply(possibilities, Some(unionRef)) + UnionDeclaration(
            name,
            ifaceFields,
            possibilities.map {
              case Scala.CaseObject(nme, _) =>
                CustomTypeRef(nme, ListSet.empty)

              case Scala.CaseClass(n, _, _, tpeArgs) => {
                CustomTypeRef(
                  buildInterfaceName(n),
                  tpeArgs.map { SimpleTypeRef(_) }
                )
              }

              case m =>
                CustomTypeRef(
                  buildInterfaceName(m.name),
                  ListSet.empty
                )
            },
            superInterface)
        }
      }
    }

  private def transpileInterface(
    scalaClass: Scala.CaseClass,
    superInterface: Option[InterfaceDeclaration]) = InterfaceDeclaration(
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

  private def transpileClass(
    scalaClass: Scala.CaseClass,
    superInterface: Option[InterfaceDeclaration]) = {
    Typescript.ClassDeclaration(
      scalaClass.name,
      ClassConstructor(
        scalaClass.fields map { scalaMember =>
          ClassConstructorParameter(
            scalaMember.name,
            transpileTypeRef(scalaMember.typeRef, inInterfaceContext = false))
        }),
      values = scalaClass.values.map { v =>
        Typescript.Member(v.name, transpileTypeRef(v.typeRef, false))
      },
      typeParams = scalaClass.typeArgs,
      superInterface)
  }

  private def transpileTypeRef(
    scalaTypeRef: Scala.TypeRef,
    inInterfaceContext: Boolean
  ): Typescript.TypeRef = scalaTypeRef match {
    case Scala.IntRef =>
      Typescript.NumberRef
    case Scala.LongRef =>
      Typescript.NumberRef
    case Scala.DoubleRef =>
      Typescript.NumberRef
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
      Typescript.DateRef
    case Scala.DateTimeRef =>
      Typescript.DateTimeRef

    case Scala.TypeParamRef(name) =>
      Typescript.SimpleTypeRef(name)

    case Scala.OptionRef(innerType) if (
      config.optionToNullable && config.optionToUndefined) =>
      Typescript.UnionType(ListSet(
        Typescript.UnionType(ListSet(
          transpileTypeRef(innerType, inInterfaceContext), NullRef)),
        UndefinedRef))

    case Scala.OptionRef(innerType) if config.optionToNullable =>
      Typescript.UnionType(ListSet(
        transpileTypeRef(innerType, inInterfaceContext),
        NullRef))

    case Scala.MapRef(kT, vT) => Typescript.MapType(
      transpileTypeRef(kT, inInterfaceContext),
      transpileTypeRef(vT, inInterfaceContext))

    case Scala.UnionRef(possibilities) =>
      Typescript.UnionType(possibilities.map { i =>
        transpileTypeRef(i, inInterfaceContext)
      })

    case Scala.OptionRef(innerType) if config.optionToUndefined =>
      Typescript.UnionType(ListSet(
        transpileTypeRef(innerType, inInterfaceContext),
        UndefinedRef))

    case Scala.UnknownTypeRef(_) =>
      Typescript.StringRef
  }
}
