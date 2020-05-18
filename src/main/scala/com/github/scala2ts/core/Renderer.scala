package com.github.scala2ts.core

import java.time.OffsetDateTime

import com.github.scala2ts.BuildInfo._
import com.github.scala2ts.configuration.{Configuration, SealedTypesMapping}
import com.github.scala2ts.model.Typescript._
import Internals.list

import scala.collection.immutable.ListSet

object Renderer {

  def apply(config: Configuration, decls: ListSet[Declaration]): String =
    s"""${makeHeadline}
       |
       |${decls.map(makeDeclaration(config)).filter(_.nonEmpty).mkString("\n\n")}
       |""".stripMargin

  private[this] def makeHeadline: String =
    s"""/**
       | * Scala2TS-Generated Typescript Definitions
       | * Scala2TS: $version, Scala : $scalaVersion, SBT: $sbtVersion
       | * Created at ${OffsetDateTime.now()}
       | */""".stripMargin

  private[this] def makeDeclaration(config: Configuration)(decl: Declaration): String = decl match {
    case enum: EnumerationDeclaration => makeEnum(enum)
    case iface: InterfaceDeclaration => makeInterface(iface)
    case union: UnionDeclaration => makeUnion(union)
    case tpeUnion: TypeUnionDeclaration => makeTypeUnion(tpeUnion, config)
    case _ => ""
  }

  private[this] def makeEnum(enum: EnumerationDeclaration): String =
    s"""export enum ${enum.name} {
       |  ${enum.values.map(e => s"$e = '$e'").mkString(",\n  ")}
       |}""".stripMargin

  private[this] def makeUnion(union: UnionDeclaration): String =
    if (union.fields.isEmpty) s"export interface ${union.name}${makeSuper(union.superInterface)} { }"
    else
      s"""export interface ${union.name}${makeSuper(union.superInterface)} {
          |  ${list(union.fields).map(makeField).mkString("\n  ")}
          |}""".stripMargin

  private[this] def makeTypeUnion(tpeUnion: TypeUnionDeclaration, config: Configuration): String =
    s"""export type ${tpeUnion.name} =
       |  ${tpeUnion.values.map(makeTypeUnionItem(config)).mkString(" |\n  ")} ;
       |""".stripMargin

  private[this] def makeInterface(interface: InterfaceDeclaration): String =
    if (interface.fields.isEmpty) s"export interface ${interface.name}${makeTypeArgs(interface.typeParams)}${makeSuper(interface.superInterface)} { }"
    else
      s"""export interface ${interface.name}${makeTypeArgs(interface.typeParams)}${makeSuper(interface.superInterface)} {
         |  ${list(interface.fields).map(makeField).mkString("\n  ")}
         |}""".stripMargin

  private[this] def makeField(member: Member): String = member.typeRef match {
    case arr: ArrayRef =>
      s"${member.name}: Array<${makeType(arr.innerType)}>;"
    case option: OptionRef =>
      s"${member.name}?: ${makeType(option.typeRef)};"
    case other: CustomTypeRef =>
      s"${member.name}: ${other.name}${makeTypeArgs(other.typeArgs.map(makeType))};"
    case _ =>
      s"${member.name}: ${member.typeRef};"
  }

  private[this] def makeType(ref: TypeRef): String = ref match {
    case arr: ArrayRef =>
      s"Array<${makeType(arr.innerType)}>"
    case option: OptionRef =>
      s"${makeType(option.typeRef)} | null"
    case union: UnionRef =>
      union.possibilities.mkString(" | ")
    case map: MapRef =>
      s"{ [key: ${makeType(map.keyType)}]: ${makeType(map.valueType)} }"
    case other: CustomTypeRef =>
      s"${other.name}${makeTypeArgs(other.typeArgs.map(_.toString))}"
    case simple: SimpleTypeRef => simple.name
    case _ => ref.toString
  }

  private[this] def makeSuper(superInterface: Option[InterfaceDeclaration], verb: String = "extends"): String =
    if (superInterface.isEmpty) ""
    else s" $verb ${superInterface.get.name}"

  private[this] def makeTypeArgs(args: ListSet[String]): String = {
    if (args.isEmpty) ""
    else s"<${args.mkString(", ")}>"
  }

  private[this] def makeTypeUnionItem(config: Configuration)(name: String): String = {
    if (config.sealedTypesMapping == SealedTypesMapping.AsUnionString) {
      s"'$name'"
    } else { name }
  }
}