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
    scalaTypes.flatMap[Declaration, ListSet[Declaration]] {
      case Scala.ScalaEnum(name, values) => ListSet(EnumerationDeclaration(
        name,
        values
      ))
      case Scala.EnumerationEnum(name, values) => ListSet(EnumerationDeclaration(
        name,
        values
      ))

      case scalaClass: Scala.CaseClass if !withinUnion(scalaTypes, scalaClass) =>
        ListSet(transpileInterface(scalaClass, superInterface))

      case Scala.SealedUnion(name, fields, possibilities) =>
        val iFaceFields = fields.map { member =>
          Member(
            member.name,
            transpileTypeRef(member.typeRef, inInterfaceContext = false)
          )
        }

        val unionRef = InterfaceDeclaration(
          buildInterfaceName(name),
          iFaceFields,
          ListSet.empty,
          superInterface
        )

        apply(possibilities, Some(unionRef)) + UnionDeclaration(
          name,
          iFaceFields,
          possibilities.map {
            case Scala.CaseObject(nme, _) =>
              CustomTypeRef(buildInterfaceName(nme), ListSet.empty)

            case Scala.CaseClass(n, _, _, tpeArgs) =>
              CustomTypeRef(buildInterfaceName(n), tpeArgs.map(SimpleTypeRef))

            case m =>
              CustomTypeRef(buildInterfaceName(m.name), ListSet.empty)
          },
          superInterface
        )
      case _ => ListSet.empty
    }

  private def transpileInterface(
    scalaClass: Scala.CaseClass,
    superInterface: Option[InterfaceDeclaration]
  ): InterfaceDeclaration = InterfaceDeclaration(
    buildInterfaceName(scalaClass.name),
    scalaClass.fields.map { scalaMember =>
      Typescript.Member(
        scalaMember.name,
        transpileTypeRef(scalaMember.typeRef, inInterfaceContext = true))
    },
    typeParams = scalaClass.typeArgs,
    superInterface = superInterface
  )

  private def buildInterfaceName(name: String) =
    s"${config.typeNamePrefix}${name}${config.typeNameSuffix}"

  private def withinUnion(types: ListSet[Scala.TypeDef], tpe: Scala.TypeDef): Boolean =
    types exists {
      case union: Scala.SealedUnion => union.possibilities.exists { _.name == tpe.name }
      case _ => false
    }

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

    case Scala.EnumRef(name) =>
      Typescript.SimpleTypeRef(name)

    case Scala.OptionRef(innerType) =>
      Typescript.OptionRef(transpileTypeRef(innerType, inInterfaceContext))

    case Scala.MapRef(kT, vT) => Typescript.MapRef(
      transpileTypeRef(kT, inInterfaceContext),
      transpileTypeRef(vT, inInterfaceContext))

    case Scala.UnionRef(possibilities) =>
      Typescript.UnionRef(possibilities.map { i =>
        transpileTypeRef(i, inInterfaceContext)
      })

    case Scala.UnknownTypeRef(_) =>
      Typescript.StringRef
  }
}
