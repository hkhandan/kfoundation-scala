// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.db.postgres

import net.kfoundation.scala.UString
import net.kfoundation.scala.db.{DSL, DSLInterpreter, DatabaseError}
import net.kfoundation.scala.serialization.{ObjectSerializer, SerializationError}


object PostgresDSLInterpreter extends DSLInterpreter {

  private class FieldValueSerializer(forLike: Boolean = false) extends ObjectSerializer {
    private var _value: Option[UString] = None

    def value: UString = _value.getOrElse(
      throw new DatabaseError("Field value is not deserialized"))

    override def writePropertyName(name: UString): ObjectSerializer =
      throw new SerializationError("Complex values are not supported")

    override def writeLiteral(value: UString): ObjectSerializer = {
      _value = if(forLike) {
         Some(s"'%$value%'")
      } else {
        Some(s"'$value'")
      }
      this
    }

    override def writeLiteral(value: Long): ObjectSerializer = {
      _value = Some(UString.of(value))
      this
    }

    override def writeLiteral(value: Double): ObjectSerializer = {
      _value = Some(UString.of(value))
      this
    }

    override def writeLiteral(value: Boolean): ObjectSerializer = {
      _value = if(value) Some(TRUE) else Some(FALSE)
      this
    }

    override def writeNull(): ObjectSerializer = {
      _value = Some(NULL)
      this
    }

    override def writeObjectBegin(name: UString): ObjectSerializer =
      throw new SerializationError("Complex values are not supported")

    override def writeObjectEnd(): ObjectSerializer =
      throw new SerializationError("Complex values are not supported")

    override def writeCollectionBegin(): ObjectSerializer =
      throw new SerializationError("Complex values are not supported")

    override def writeCollectionEnd(): ObjectSerializer =
      throw new SerializationError("Complex values are not supported")

    override def writeStreamEnd(): Unit = {}
  }


  private val TRUE: UString = "true"
  private val FALSE: UString = "false"
  private val NULL: UString = "null"
  private val D_QUOTE: UString = "\""
  private val EQ: UString = "="


  private val keywords: Set[String] = Set("ALL", "ANALYSE", "ANALYZE", "AND",
    "ANY", "ARRAY", "AS", "ASC", "ASYMMETRIC", "AUTHORIZATION", "BETWEEN",
    "BOTH", "CASE", "CAST", "CHECK", "COLLATE", "COLUMN", "CONSTRAINT",
    "CREATE", "CURRENT_DATE", "CURRENT_ROLE", "CURRENT_TIME", "CURRENT_TIMESTAMP",
    "CURRENT_USER", "DEFAULT", "DEFERRABLE", "DESC", "DISTINCT", "DO", "ELSE",
    "EXCEPT", "FALSE", "FOR", "FOREIGN", "FROM", "GRANT", "GROUP", "HAVING",
    "ILIKE", "IN", "INITIALLY", "INTERSECT", "INTO", "IS", "ISNULL", "JOIN",
    "LEADING", "LEFT", "LIKE", "LIMIT", "LOCALTIME", "LOCALTIMESTAMP", "NATURAL",
    "NEW", "NOT", "NOTNULL", "NULL", "OFF", "OFFSET", "OLD", "ON", "ONLY", "OR",
    "ORDER", "OUTER", "OVERLAPS", "PLACING", "PRIMARY", "REFERENCES", "RIGHT",
    "SELECT", "SESSION_USER", "SIMILAR", "SOME", "SYMMETRIC", "TABLE", "THEN",
    "TO", "TRAILING", "TRUE", "UNION", "UNIQUE", "USER", "USING", "VERBOSE",
    "WHEN", "WHERE")


  private def symbol(str: UString): UString =
    if(keywords.contains(str.toUpperCase)) {
      UString.join(D_QUOTE, str, D_QUOTE)
    } else {
      str
    }


  private def agr(value: DSL.AggregateFunction): UString = value match {
    case DSL.AVERAGE => "avg"
    case DSL.COUNT => "count"
    case DSL.MAX => "max"
    case DSL.MIN => "min"
    case DSL.SUM => "sum"
    case _ =>
      throw new RuntimeException("Unrecognized aggregate function: " + value)
  }


  override def append(builder: UString.Builder, predicate: DSL.Predicate): Unit = predicate match {
    case DSL.TRUE => builder.append(TRUE)
    case DSL.FALSE => builder.append(FALSE)
    case e: DSL.Comparison[_] =>
      val sr = new FieldValueSerializer(e.op == DSL.LIKE)
      e.serializeRhs(sr)
      builder.append(symbol(e.lhs.name))
        .append(EQ)
        .append(sr.value)

    case a: DSL.And =>
      append(builder, " and ", a.predicates)

    case o: DSL.Or =>
      append(builder, " or ", o.predicates)

    case _ =>
      throw new DatabaseError("Unrecognized predicate: " + predicate)
  }


  private def append(builder: UString.Builder, op: UString,
    items: Seq[DSL.Predicate]): Unit =
  {
    val isCompound: Boolean = items.size > 1
    if(items.nonEmpty) {
      if(isCompound) builder.append("(")
      append(builder, items.head)
      items.tail.foreach(p => {
        builder.append(op)
        append(builder, p)
      })
      if(isCompound) builder.append(")")
    }
  }


  override def append(builder: UString.Builder, delete: DSL.Delete): Unit =
    builder.append("delete from ")
      .append(symbol(delete.table.name))
      .append(" where ")
      .use(append(_, delete.condition))


  override def append(builder: UString.Builder, select: DSL.Select): Unit = {
    builder.append("select * from ")
      .append(symbol(select.table.name))

    select.condition.foreach(c => {
      builder.append(" where ")
      append(builder, c)
    })

    select.limit.foreach(
      l => builder.append(" limit ").append(l))

    select.offset.foreach(
      o => builder.append(" offset ").append(o))
  }


  override def append(builder: UString.Builder, aggregate: DSL.Aggregate): Unit = {
    builder.append("select ")
      .append(agr(aggregate.function))
      .append("(")
      .append(symbol(aggregate.field.name))
      .append(") from ")
      .append(symbol(aggregate.table.name))

    aggregate.condition.foreach(c => {
      builder.append(" where ")
      append(builder, c)
    })
  }
}



