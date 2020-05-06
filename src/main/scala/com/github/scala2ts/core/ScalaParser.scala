package com.github.scala2ts.core

import java.sql.Timestamp
import java.time.{Instant, LocalDate, LocalDateTime, LocalTime, OffsetDateTime, ZonedDateTime}
import java.util.Date

import enumeratum.EnumEntry

import scala.collection.immutable.ListSet
import scala.reflect.api.Universe

final class ScalaParser[U <: Universe](universe: U) {
  import com.github.scala2ts.model.Scala.{TypeRef => ScalaTypeRef, _}
  import universe.{
    MethodSymbol,
    NullaryMethodType,
    Type,
    TypeRef,
    typeOf
  }

  def parseTypes(types: List[Type]): ListSet[TypeDef] =
    parse(
      types,
      ListSet.empty[Type],
      ListSet.empty[TypeDef]
    )

  private def parseType(tpe: Type): Option[TypeDef] = tpe match {
    case _ if (
      (tpe.getClass.getName contains "ModuleType") &&
      tpe <:< typeOf[Enumeration]
    ) => parseScalaEnum(tpe)

    case _ if (
      tpe.typeSymbol.isClass &&
      tpe.typeParams.isEmpty && ((
        tpe.typeSymbol.asClass.isTrait &&
        tpe.typeSymbol.asClass.isSealed
      ) || (
        tpe.typeSymbol.asClass.isAbstract &&
        tpe.typeSymbol.asClass.isSealed
      )) && tpe <:< typeOf[EnumEntry]
    ) => parseEnumerationEnum(tpe)

    case _ if (
      tpe.typeSymbol.isClass &&
      tpe.typeParams.isEmpty &&
      tpe.typeSymbol.asClass.isTrait &&
      tpe.typeSymbol.asClass.isSealed
    ) => parseSealedUnion(tpe)

    case _ if (
      tpe.typeSymbol.isClass &&
      isCaseClass(tpe) &&
      !isAnyValChild(tpe)
    ) => parseCaseClass(tpe)

    case _ => Option.empty[TypeDef]
  }

  private object Field {
    def unapply(m: MethodSymbol): Option[MethodSymbol] = m match {
      case m: MethodSymbol if (!m.isAbstract && m.isPublic && !m.isImplicit &&
        m.paramLists.forall(_.isEmpty) &&
        {
          val n = m.name.toString
          !(n.contains("$") || n.startsWith("<"))
        } &&
        m.overrides.forall { o =>
          val declaring = o.owner.fullName

          !declaring.startsWith("java.") &&
            !declaring.startsWith("scala.")
        }) => Some(m)

      case _ => None
    }
  }

  private def parseScalaEnum(enum: Type): Option[ScalaEnum] = {
    val values = enum.members.filter(_.isTerm).map(_.asTerm).filter(sym => (
      sym.typeSignature <:< typeOf[Enumeration#Value] &&
      sym.isVal
    )).map(_.name.toString.trim)

    Some(ScalaEnum(
      enum.typeSymbol.name.toString.trim,
      ListSet.empty ++ values
    ))
  }

  private def parseEnumerationEnum(enum: Type): Option[EnumerationEnum] = {
    Some(EnumerationEnum(
      enum.typeSymbol.name.toString.trim,
      ListSet.empty ++ enum.typeSymbol.asClass.knownDirectSubclasses.map(_.name.toString)
    ))
  }

  private def parseSealedUnion(tpe: Type): Option[SealedUnion] = {
    val members = tpe.decls.collect {
      case m: MethodSymbol if (m.isAbstract && m.isPublic && !m.isImplicit &&
        !m.name.toString.endsWith("$")) => member(m, List.empty)
    }

    Some(SealedUnion(
      tpe.typeSymbol.name.toString,
      ListSet.empty ++ members,
      parseTypes(tpe.typeSymbol.asClass.knownDirectSubclasses.map(_.typeSignature).toList)
    ))
  }

  private def parseCaseClass(caseClassType: Type): Option[CaseClass] = {
    val typeParams = caseClassType
      .typeConstructor
      .dealias
      .typeParams
      .map(_.name.decodedName.toString)

    // Members
    val members = caseClassType.members.collect {
      case Field(m) if m.isCaseAccessor =>
        member(m, typeParams)
    }.toList

    val values = caseClassType.decls.collect {
      case Field(m) =>
        member(m, typeParams)
    }.filterNot(members.contains)

    Some(CaseClass(
      caseClassType.typeSymbol.name.toString,
      ListSet.empty ++ members,
      ListSet.empty ++ values,
      ListSet.empty ++ typeParams
    ))
  }

  @inline private def member(
    sym: MethodSymbol,
    typeParams: List[String]
  ): TypeMember = TypeMember(
    sym.name.toString,
    scalaTypeRef(
      sym.returnType.map(_.dealias),
      typeParams.toSet
    ))

  @annotation.tailrec
  private def parse(
    types: List[Type],
    examined: ListSet[Type],
    parsed: ListSet[TypeDef]
  ): ListSet[TypeDef] = types match {
    case scalaType :: tail =>
      if (
        !examined.contains(scalaType) &&
        !scalaType.typeSymbol.isParameter
      ) {

        val relevantMemberSymbols = scalaType.members.collect {
          case m: MethodSymbol if m.isCaseAccessor => m
        }

        val memberTypes = relevantMemberSymbols.map(
          _.typeSignature.map(_.dealias) match {
            case NullaryMethodType(resultType) => resultType
            case t => t.map(_.dealias)
          })

        val typeArgs = scalaType match {
          case t: TypeRef =>
            t.args

          case _ => ListSet.empty[Type]
        }

        parse(
          memberTypes ++: typeArgs ++: tail,
          examined + scalaType,
          parsed ++ parseType(scalaType)
        )

      } else {
        parse(
          tail,
          examined + scalaType,
          parsed ++ parseType(scalaType)
        )
      }

    case _ => parsed
  }

  /**
   * It looks like you can't call typeOf on existentials or you'll throw
   * some generic reflection error, instead plug with Any
   * @see https://github.com/sksamuel/scapegoat/pull/163/files
   */
  private def scalaTypeRef(scalaType: Type, typeParams: Set[String]): ScalaTypeRef = {
    System.out.println(scalaType.typeSymbol.name.toString)
    if (isOfType(scalaType)(
      typeOf[Int],
      typeOf[Byte],
      typeOf[Short]
    )) {
      IntRef
    } else if (isOfType(scalaType)(
      typeOf[Long]
    )) {
      LongRef
    } else if (isOfType(scalaType)(
      typeOf[Double]
    )) {
      DoubleRef
    } else if (isOfType(scalaType)(
      typeOf[Boolean]
    )) {
      BooleanRef
    } else if (isOfType(scalaType)(
      typeOf[String]
    )) {
      StringRef
    } else if (isOfSubType(scalaType)(
      typeOf[List[Any]],
      typeOf[Seq[Any]],
      typeOf[ListSet[Any]],
      typeOf[Set[Any]]
    )) {
      val innerType = scalaType.asInstanceOf[TypeRef].args.head
      SeqRef(scalaTypeRef(innerType, typeParams))
    } else if (isOfSubType(scalaType)(
      typeOf[Option[Any]]
    )) {
      System.out.println("Got option")
      val innerType = scalaType.asInstanceOf[TypeRef].args.head
      OptionRef(scalaTypeRef(innerType, typeParams))
    } else if (isOfType(scalaType)(
      typeOf[Date],
      typeOf[Instant],
      typeOf[LocalDate],
      typeOf[LocalTime],
      typeOf[Timestamp],
      typeOf[LocalDateTime],
      typeOf[ZonedDateTime],
      typeOf[OffsetDateTime]
    )) {
      DateRef
    } else if (typeParams.contains(scalaType.typeSymbol.name.toString)) {
      TypeParamRef(scalaType.typeSymbol.name.toString)
    } else if (isAnyValChild(scalaType)) {
      scalaTypeRef(scalaType.members.filter(!_.isMethod).map(_.typeSignature).head, Set())
    } else if (isCaseClass(scalaType)) {
      val caseClassName = scalaType.typeSymbol.name.toString
      val typeArgs = scalaType.asInstanceOf[TypeRef].args
      val typeArgRefs = typeArgs.map(scalaTypeRef(_, typeParams))

      CaseClassRef(caseClassName, ListSet.empty ++ typeArgRefs)
    } else if (isOfSubType(scalaType)(
      typeOf[Either[Any, Any]]
    )) {
      val innerTypeL = scalaType.asInstanceOf[TypeRef].args.head
      val innerTypeR = scalaType.asInstanceOf[TypeRef].args.last

      UnionRef(ListSet(
        scalaTypeRef(innerTypeL, typeParams),
        scalaTypeRef(innerTypeR, typeParams)))
    } else if (isOfSubType(scalaType)(
      typeOf[Map[Any, Any]]
    )) {
      val keyType = scalaType.asInstanceOf[TypeRef].args.head
      val valueType = scalaType.asInstanceOf[TypeRef].args.last
      MapRef(scalaTypeRef(keyType, typeParams), scalaTypeRef(valueType, typeParams))
    } else if (isOfSubType(scalaType)(
      typeOf[Enumeration],
      typeOf[EnumEntry]
    )) {
      EnumRef(scalaType.typeSymbol.name.toString)
    } else {
      UnknownTypeRef(scalaType.typeSymbol.name.toString)
    }
  }

  private def isOfType(tpe: Type)(types: Type*): Boolean =
    types.exists(t => tpe =:= t)

  private def isOfSubType(tpe: Type)(subtypes: Type*): Boolean =
    subtypes.exists(t => tpe <:< t)

  private def isCaseClass(scalaType: Type): Boolean =
    scalaType.typeSymbol.isClass && scalaType.typeSymbol.asClass.isCaseClass

  private def isAnyValChild(scalaType: Type): Boolean =
    scalaType <:< typeOf[AnyVal]

}
