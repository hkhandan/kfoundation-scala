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
import net.kfoundation.scala.serialization.{ValueReadWriter, ValueReadWriters}
import net.kfoundation.scala.util.WQName

import scala.collection.mutable



object MultiDictionary {
  import ValueReadWriters._

  /** Object model for fallback path */
  class Fallback(val from: Dialect, val to: Dialect) {
    override def equals(other: Any): Boolean = other match {
      case that: Fallback =>
          from == that.from &&
          to == that.to
      case _ => false
    }
  }

  /** Dictionary entry */
  class Entry(val lang: Dialect, val value: UString)

  /** Set of different translations of the same phrase */
  class EntrySet(val key: UString, val values: Seq[Entry])

  /** Well-qualified set of dictionary entries */
  class DomainSet(val name: WQName, val entries: Seq[EntrySet])

  /** Flatted dictionary record. Mainly used for merge operation. */
  class Record(val domain: WQName, val key: UString, val dialect: Dialect,
    val value: UString)


  val EMPTY = new MultiDictionary(Dialect.EN_US, Nil, Nil)
  val DOMAIN_RW: ValueReadWriter[WQName] = STRING.map(WQName(_), _.toUString)


  def fallbackRw(dialectRw: ValueReadWriter[Dialect]): ValueReadWriter[Fallback] =
    tuple("Fallback", "from"->dialectRw, "to"->dialectRw)
      .map(t => new Fallback(t._1, t._2), s => (s.from, s.to))


  def entryRw(dialectRw: ValueReadWriter[Dialect]): ValueReadWriter[Entry] =
    tuple("Entry", "lang"->dialectRw, "value"->STRING)
      .map(t => new Entry(t._1, t._2), s => (s.lang, s.value))


  def entrySetRw(dialectRw: ValueReadWriter[Dialect]): ValueReadWriter[EntrySet] =
    tuple("EntrySet", "key"->STRING, "values" -> entryRw(dialectRw).seq)
      .map(t => new EntrySet(t._1, t._2), s => (s.key, s.values))


  def domainSetRw(dialectRw: ValueReadWriter[Dialect]): ValueReadWriter[DomainSet] =
    tuple(
      "Domain",
      "name"->DOMAIN_RW,
      "entries"->entrySetRw(dialectRw).seq)
      .map(t => new DomainSet(t._1, t._2), s => (s.name, s.entries))


  def recordRw(dialectRw: ValueReadWriter[Dialect]): ValueReadWriter[Record] =
    tuple(
      "Record",
      "domain"->DOMAIN_RW,
      "key"->STRING,
      "dialect"->dialectRw,
      "value"->STRING)
    .map(
      t => new Record(t._1, t._2, t._3, t._4),
      s => (s.domain, s.key, s.dialect, s.value))


  def rw(dialectFactory: Dialect.DialectFactory): ValueReadWriter[MultiDictionary] =
    rw(Dialect.rw(dialectFactory))


  def rw(dialectRw: ValueReadWriter[Dialect]): ValueReadWriter[MultiDictionary] = {
    tuple("MultiDictionary",
      "default"->dialectRw.option,
      "fallbacks"->fallbackRw(dialectRw).seq,
      "domains"->domainSetRw(dialectRw).seq)
      .map(
        t => new MultiDictionary(t._1.getOrElse(Dialect.EN_US), t._2, t._3),
        s => (Some(s.defaultDialect), s.fallbacks, s.domains))
  }

  /** Converts a Dictionry into a MultiDictionary */
  def apply(dict: Dictionary) = new MultiDictionary(
    dict.dialect,
    Seq.empty,
    dict.domains.map(d => new DomainSet(
      d.name,
      d.entries.map(e => new EntrySet(
        e.key,
        Seq(new Entry(dict.dialect, e.value)) )))))


  /** Constructs a structured set of record out of given set of flat records */
  def fold(records: Seq[Record]): Seq[DomainSet] = {
    val byDomain = records.groupBy(_.domain)
    val domains = byDomain.map(domain => {
      val byKey = domain._2.groupBy(_.key)
      val entrySets = byKey.map(key => {
        val entries = key._2.map(e => new Entry(e.dialect, e.value))
        new EntrySet(key._1, entries)
      })
      new DomainSet(domain._1, entrySets.toSeq)
    })
    domains.toSeq
  }
}


/**
 * Serializable object model containing all the data needed by localizers.
 */
class MultiDictionary(val defaultDialect: Dialect,
  val fallbacks: Seq[MultiDictionary.Fallback],
  val domains: Seq[MultiDictionary.DomainSet])
{
  import MultiDictionary._

  /**
   * Finds translation of the phrase with given name into the given language.
   */
  private def find(wqName: WQName, lang: Dialect): Option[UString] = {
    val domain = wqName.parent
    val key = wqName.last
    domains.find(domain.isEmpty || _.name.equals(domain))
      .flatMap(_.entries.find(_.key.equals(key))
        .flatMap(_.values.find(_.lang.equals(lang))
          .map(_.value)))
  }

  /**
   * Attempts to find the translation of the phrase with the given name in the
   * desired language. Returns the last segment of wqName, if none found.
   */
  def lookup(wqName: WQName, lang: Dialect): UString =
    find(wqName, lang).getOrElse(
      if(lang == defaultDialect) {
        wqName.toUString
      } else {
        val target = fallbacks.find(_.from==lang)
          .map(_.to)
          .getOrElse(defaultDialect)
        lookup(wqName, target)
      })


  /** Converts this dictionary into a set of flat records. */
  def flatten: Seq[Record] = {
    val list = new mutable.ArrayBuffer[Record]
    domains.foreach(
      domain => domain.entries.foreach(
        entrySet => entrySet.values.foreach(
          entry => list.addOne(
            new Record(domain.name, entrySet.key, entry.lang, entry.value)))))
    list.toSeq
  }


  /** Merges this dictionary with another one. */
  def merge(other: MultiDictionary): MultiDictionary = {
    val _domains = fold(flatten.appendedAll(other.flatten))
    val _fallbacks = fallbacks.appendedAll(other.fallbacks).distinct
    new MultiDictionary(defaultDialect, _fallbacks, _domains)
  }
}