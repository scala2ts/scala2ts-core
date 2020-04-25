package com.github.scala2ts.core

import scala.collection.immutable.ListSet

private[core] object Internals {
  def list[T](set: ListSet[T]): List[T] = set.toList.reverse
}