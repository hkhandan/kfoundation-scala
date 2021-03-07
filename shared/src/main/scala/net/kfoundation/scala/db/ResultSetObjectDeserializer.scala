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
import net.kfoundation.scala.parse.CodeLocation
import net.kfoundation.scala.serialization.ObjectDeserializer
import net.kfoundation.scala.serialization.internals.ObjectStreamStateMachine

import java.sql.ResultSet


class ResultSetObjectDeserializer(rs: ResultSet) extends ObjectDeserializer {
  private val stateMachine = new ObjectStreamStateMachine

  private val metadata = rs.getMetaData
  private val nFields = metadata.getColumnCount
  private var currentColumn = 1


  override def tryReadNullLiteral(): Boolean = {
    rs.getString(currentColumn)
    if(rs.wasNull()) {
      stateMachine.literal()
      currentColumn += 1
      true
    } else {
      false
    }
  }

  override def readObjectBegin(): Option[UString] = {
    stateMachine.objectBegin()
    currentColumn = 1
    None
  }

  override def readObjectEnd(): Option[UString] = {
    stateMachine.objectEnd()
    None
  }

  override def readCollectionBegin(): Unit = {
    stateMachine.collectionBegin()
  }


  override def tryReadCollectionEnd(): Boolean = {
    if(!rs.next()) {
      stateMachine.collectionEnd()
      true
    } else {
      false
    }
  }


  override def tryReadPropertyName(): Option[UString] =
    if(currentColumn <= nFields) {
      stateMachine.property()
      Some(UString.of(metadata.getColumnLabel(currentColumn)))
    } else {
      None
    }


  override def readStringLiteral(): UString = {
    stateMachine.literal()
    val str = UString.of(rs.getString(currentColumn))
    currentColumn += 1
    str
  }


  override def readIntegerLiteral(): Long = {
    stateMachine.literal()
    val i = rs.getLong(currentColumn)
    currentColumn += 1
    i
  }


  override def readDecimalLiteral(): Double = {
    stateMachine.literal()
    val d = rs.getDouble(currentColumn)
    currentColumn += 1
    d
  }


  override def readBooleanLiteral(): Boolean = {
    stateMachine.literal()
    val b = rs.getBoolean(currentColumn)
    currentColumn += 1
    b
  }


  override protected def getCurrentLocation: CodeLocation =
    new CodeLocation("ResultSet")
}
