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
import net.kfoundation.scala.UString.Interpolator
import net.kfoundation.scala.serialization.internals.ObjectStreamStateMachine
import net.kfoundation.scala.serialization.{ObjectSerializer, SerializationError}


object AbstractQueryObjectSerializer {
  private object NULL_VALUE
  private val NULL: UString = "null"
  private val TRUE: UString = "true"
  private val FALSE: UString = "false"

  private def escape(str: String): String = str.replace("\\", "\\\\")
    .replace("\t", "\\t")
    .replace("\b", "\\b")
    .replace("\n", "\\n")
    .replace("\r", "\\r")
    .replace("\f", "\\f")
    .replace("\'", "''")
}


abstract class AbstractQueryObjectSerializer extends ObjectSerializer {
  import AbstractQueryObjectSerializer._

  private val stateMachine = new ObjectStreamStateMachine

  private var tableName: Option[UString] = None
  private var path: Seq[UString] = Seq.empty
  private var fields = Seq[(UString, Any)]()


  private def writeAnyLiteral(value: Any): ObjectSerializer = {
    stateMachine.literal()
    fields = fields :+ (UString.join(path, "_"), value)
    path = path.dropRight(1)
    this
  }


  private def flush(): Unit = {
    import UString.of

    val table = tableName.getOrElse(throw new SerializationError(
      "Only a proper object can be serialized to an insert query"))

    val normalizedFields = fields.map(t => (t._1, t._2 match {
      case str: UString => U"'${escape(str)}'"
      case i: Int => of(i)
      case l: Long => of(l)
      case d: Double => of(d)
      case b: Boolean => if (b) TRUE else FALSE
      case NULL_VALUE => NULL
      case any => of(any.toString)
    }))

    write(table, normalizedFields)
  }


  override def writePropertyName(name: UString): ObjectSerializer = {
    stateMachine.property()
    path = path :+ name
    this
  }


  override def writeLiteral(value: UString): ObjectSerializer =
    writeAnyLiteral(value)


  override def writeLiteral(value: Long): ObjectSerializer =
    writeAnyLiteral(value)


  override def writeLiteral(value: Double): ObjectSerializer =
    writeAnyLiteral(value)


  override def writeLiteral(value: Boolean): ObjectSerializer =
    writeAnyLiteral(value)


  override def writeNull(): ObjectSerializer = writeAnyLiteral(NULL_VALUE)


  override def writeObjectBegin(name: UString): ObjectSerializer = {
    stateMachine.objectBegin(name)
    if (tableName.isEmpty) {
      tableName = Some(name)
    }
    this
  }


  override def writeObjectEnd(): ObjectSerializer = {
    stateMachine.objectEnd()
    if (path.nonEmpty) {
      path = path.dropRight(1)
    } else if (tableName.nonEmpty) {
      flush()
    }
    this
  }


  override def writeCollectionBegin(): ObjectSerializer =
    throw new SerializationError("Collections are not supported")


  override def writeCollectionEnd(): ObjectSerializer =
    throw new SerializationError("Collections are not supported")


  override def writeStreamEnd(): Unit = stateMachine.streamEnd()


  protected def write(table: UString, fields: Seq[(UString, UString)]): Unit
}
