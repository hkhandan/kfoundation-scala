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
import net.kfoundation.scala.serialization.ValueReadWriter



object LMessage {
  import net.kfoundation.scala.serialization.ValueReadWriters._

  implicit val RW: ValueReadWriter[LMessage] = tuple(
    "LString",
    "key" -> STRING,
    "params" -> tuple("Param", "name"->STRING, "value"->STRING).seq)
    .map(
      t => new LMessage(t._1, t._2.toMap),
      s => (s.key, s.params.toSeq))

  /** Constructs  new LMessage with the given key and parameters. */
  def apply(key: UString, params: (UString, UString)*) =
    new LMessage(key, params:_*)
}



/**
 * An LMessage carries a key and set of parameters to be resolved against
 * a parameterized dictionary record. Use Localizer to translate an LMessage.
 * @param key
 * @param params
 */
class LMessage(val key: UString, val params: Map[UString, UString]) {
  def this(key: UString, params: (UString, UString)*) =
    this(key, params.toMap)

  /** Creates a new LMessage with the given parameter added to it. */
  def withParam(name: UString, value: UString): LMessage =
    new LMessage(key, params + Tuple2(name, value))

  /** Creates a new LMessage with the given parameter removed from it. */
  def withoutParam(name: UString): LMessage =
    new LMessage(key, params - name)
}