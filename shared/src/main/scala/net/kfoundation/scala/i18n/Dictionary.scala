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
import net.kfoundation.scala.i18n.Dictionary.DomainSet
import net.kfoundation.scala.serialization.ValueReadWriter
import net.kfoundation.scala.util.WQName



object Dictionary {
  import net.kfoundation.scala.serialization.ValueReadWriters._

  class Entry(val key: UString, val value: UString)
  class DomainSet(val name: WQName, val entries: Seq[Entry])

  val ENTRY_RW: ValueReadWriter[Entry] =
    tuple("Entry", "key"->STRING, "value"->STRING)
    .map(t => new Entry(t._1, t._2), s => (s.key, s.value))

  val DOMAIN_SET_RW: ValueReadWriter[DomainSet] =
    tuple("DomainSet",
      "name"->MultiDictionary.DOMAIN_RW,
      "entries"->ENTRY_RW.seq)
    .map(t => new DomainSet(t._1, t._2), s => (s.name, s.entries))

  def rw(dialect: ValueReadWriter[Dialect]): ValueReadWriter[Dictionary] =
    tuple("Dictionary", "dialect"->dialect, "domains"->DOMAIN_SET_RW.seq)
      .map(t => new Dictionary(t._1, t._2), s => (s.dialect, s.domains))
}



class Dictionary(val dialect: Dialect, val domains: Seq[DomainSet]) {

}
