package com.github.scala2ts.model

import scala.collection.immutable.ListSet

object Typescript {
  sealed trait Declaration

  sealed trait TypeRef

  case class CustomTypeRef(
    name: String,
    typeArgs: ListSet[TypeRef]
  ) extends TypeRef

  case class ArrayRef(innerType: TypeRef) extends TypeRef

  case class InterfaceDeclaration(
    name: String,
    fields: ListSet[Member],
    typeParams: ListSet[String],
    superInterface: List[InterfaceDeclaration],
    traits: ListSet[InterfaceDeclaration],
    isTrait: Boolean
  ) extends Declaration
  // TODO: Support mapping of typeParams with superInterface

  case class Member(name: String, typeRef: TypeRef)

  case class ClassDeclaration(
    name: String,
    constructor: ClassConstructor,
    values: ListSet[Member],
    typeParams: ListSet[String],
    superInterface: List[InterfaceDeclaration],
    traits: ListSet[InterfaceDeclaration]
  ) extends Declaration

  case class SingletonDeclaration(
    name: String,
    values: ListSet[Member],
    superInterface: List[InterfaceDeclaration]
  ) extends Declaration

  case class UnionDeclaration(
    name: String,
    fields: ListSet[Member],
    possibilities: ListSet[CustomTypeRef],
    superInterface: List[InterfaceDeclaration]
  ) extends Declaration

  case class EnumerationDeclaration(
    name: String,
    values: ListSet[String]
  ) extends Declaration

  case class TypeUnionDeclaration(
    name: String,
    values: ListSet[String]
  ) extends Declaration

  case class ClassConstructor(parameters: ListSet[ClassConstructorParameter])

  case class ClassConstructorParameter(
    name: String,
    typeRef: TypeRef
  )

  case class UnknownTypeRef(name: String) extends TypeRef

  case object NumberRef extends TypeRef {
    override def toString = "number"
  }

  case object StringRef extends TypeRef {
    override def toString = "string"
  }

  case object BooleanRef extends TypeRef {
    override def toString = "boolean"
  }

  case object DateRef extends TypeRef {
    override def toString = "Date"
  }

  case object NullRef extends TypeRef {
    override def toString = "null"
  }

  case object UndefinedRef extends TypeRef {
    override def toString = "undefined"
  }

  case class SimpleTypeRef(name: String) extends TypeRef {
    override def toString = name
  }

  case class OptionRef(typeRef: TypeRef) extends TypeRef

  case class UnionRef(possibilities: ListSet[TypeRef]) extends TypeRef

  case class MapRef(keyType: TypeRef, valueType: TypeRef) extends TypeRef
}
