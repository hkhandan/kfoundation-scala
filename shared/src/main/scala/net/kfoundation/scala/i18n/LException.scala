// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.i18n

import net.kfoundation.scala.UString


object LException {
  import UString._

  private def composeMessage(message: LMessage):
    UString = join(message.key, " (",
      join(message.params.map(kv => U"${kv._1}: ${kv._2}"), ", "),
      ")")
}

/**
 * LException carries an LMessage instead of a normal string. This relieves
 * the servers from translation error messages, letting the client to
 * do so.
 * @param message
 * @param cause
 */
class LException(message: LMessage, cause: Option[Throwable] = None)
extends Exception(
  LException.composeMessage(message).toString,
  cause.orNull)
{
  def this(key: UString, params: (UString, UString)*) =
    this(new LMessage(key, params.toMap), None)

  def getLocalizableMessage: LMessage = message
}
