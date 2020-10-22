package com.github.scala2ts.core

import com.github.scala2ts.configuration.{Configuration, DateMapping, LongDoubleMapping, SealedTypesMapping}
import com.github.scala2ts.model.Typescript._
import com.github.scala2ts.model.{Scala, Typescript}

import scala.collection.immutable.ListSet

final class Transpiler(config: Configuration) {
  @inline def apply(scalaTypes: ListSet[Scala.TypeDef]): ListSet[Declaration] =
    apply(scalaTypes, superInterface = List.empty)

  def apply(
    scalaTypes: ListSet[Scala.TypeDef],
    superInterface: List[InterfaceDeclaration]
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

      case scalaClass: Scala.CaseClass => {
        val interface: InterfaceDeclaration = transpileInterface(scalaClass, superInterface)
        interface.traits + interface
      }

      case scalaTrait: Scala.Trait => List(
        transpileTrait(scalaTrait)
      )

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
          superInterface,
          ListSet.empty,
          isTrait = false
        )

        val assocSealedTypeDecl = config.sealedTypesMapping match {
          case SealedTypesMapping.AsEnum => ListSet(EnumerationDeclaration(
            s"${buildInterfaceName(name)}Types",
            possibilities.map(p => buildInterfaceName(p.name))
          ))
          case SealedTypesMapping.AsUnion | SealedTypesMapping.AsUnionString => ListSet(TypeUnionDeclaration(
            s"${buildInterfaceName(name)}Types",
            possibilities.map(p => buildInterfaceName(p.name))
          ))
          case _ => ListSet.empty
        }

        apply(possibilities, List(unionRef)) + UnionDeclaration(
          name,
          iFaceFields,
          possibilities.map {
            case Scala.CaseObject(nme, _) =>
              CustomTypeRef(buildInterfaceName(nme), ListSet.empty)

            case Scala.CaseClass(n, _, _, tpeArgs, _, _) =>
              CustomTypeRef(buildInterfaceName(n), tpeArgs.map(SimpleTypeRef))

            case m =>
              CustomTypeRef(buildInterfaceName(m.name), ListSet.empty)
          },
          if (superInterface.isEmpty) List(unionRef) else List.empty
        ) ++ assocSealedTypeDecl
      case _ => ListSet.empty
    }

  private def transpileInterface(
    scalaClass: Scala.CaseClass,
    superInterface: List[InterfaceDeclaration]
  ): InterfaceDeclaration = InterfaceDeclaration(
    buildInterfaceName(scalaClass.name),
    scalaClass.fields.map { scalaMember =>
      Typescript.Member(
        scalaMember.name,
        transpileTypeRef(scalaMember.typeRef, inInterfaceContext = true))
    },
    typeParams = scalaClass.typeArgs,
    superInterface = superInterface,
    scalaClass.traits.map(transpileTrait),
    isTrait = false
  )

  private def transpileTrait(scalaTrait: Scala.Trait): InterfaceDeclaration =
    InterfaceDeclaration(
      buildInterfaceName(scalaTrait.name),
      scalaTrait.fields.map { member =>
        Typescript.Member(
          member.name,
          transpileTypeRef(member.typeRef, inInterfaceContext = true))
      },
      ListSet.empty,
      List.empty,
      ListSet.empty,
      isTrait = true
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

    case Scala.TraitRef(name) =>
      Typescript.SimpleTypeRef(name)

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

    case Scala.UnknownTypeRef(name) =>
      Typescript.CustomTypeRef(name, ListSet.empty)
  }
}
