// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.db

import net.kfoundation.scala.UString
import net.kfoundation.scala.serialization.{ObjectSerializer, ValueWriter}

import scala.language.implicitConversions


object DSL {
  sealed class Predicate {
    def and(p: Predicate): And = new And(Seq(this, p))
    def and(ps: Iterable[Predicate]): And = new And(ps.toSeq.prepended(this))
    def or(p: Predicate) = new Or(Seq(this, p))
    def or(ps: Iterable[Predicate]): Or = new Or(ps.toSeq.prepended(this))
  }

  val TRUE = new Predicate
  val FALSE = new Predicate


  sealed class AggregateFunction
  val COUNT = new AggregateFunction
  val AVERAGE = new AggregateFunction
  val MAX = new AggregateFunction
  val MIN = new AggregateFunction
  val SUM = new AggregateFunction


  sealed class ComparisonOperator
  val EQUALS = new ComparisonOperator
  val LIKE = new ComparisonOperator

  class Table(val name: UString)


  class Field(val name: UString) {
    def ===[T](value: T)(implicit writer: ValueWriter[T]) =
      new Comparison[T](EQUALS, this, value, writer)

    def like[T](value: T)(implicit writer: ValueWriter[T]) =
      new Comparison[T](EQUALS, this, value, writer)
  }


  class Comparison[T](val op: ComparisonOperator, val lhs: Field, val rhs: T, val writer: ValueWriter[T]) extends Predicate {
    def serializeRhs(serializer: ObjectSerializer): Unit =
      writer.write(serializer, rhs)
  }


  class And(val predicates: Seq[Predicate]) extends Predicate {
    override def and(p: Predicate) = new And(predicates :+ p)
    override def and(ps: Iterable[Predicate]): And =
      new And(predicates.appendedAll(ps))
  }


  class Or(val predicates: Seq[Predicate]) extends Predicate {
    override def or(p: Predicate) = new Or(predicates :+ p)
    override def or(ps: Iterable[Predicate]): Or =
      new Or(predicates.appendedAll(ps))
  }


  class Select(val table: Table, val condition: Option[Predicate],
    val limit: Option[Int] = None, val offset: Option[Int] = None)
  {
    def withLimit(value: Int) =
      new Select(table, condition, Some(value), limit)

    def withOffset(value: Int) =
      new Select(table, condition, offset, Some(value))

    def count = new Aggregate(COUNT, "1", table, condition)
  }


  class Aggregate(val function: AggregateFunction, val field: Field,
    val table: Table, val condition: Option[Predicate])


  class Delete(val table: Table, val condition: Predicate)


  def select(table: Table, condition: Predicate) =
    new Select(table, Some(condition))


  def delete(table: Table, condition: Predicate)=
    new Delete(table, condition)


  def count(table: Table, condition: Predicate) =
    new Aggregate(COUNT, "1", table, Some(condition))


  implicit def field(name: String): Field = new Field(name)
  implicit def field(name: UString): Field = new Field(name)
  implicit def table(name: String): Table = new Table(name)
  implicit def table(name: UString): Table = new Table(name)
}
