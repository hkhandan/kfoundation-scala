// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.uui


/**
 * Convenience tools used to build Seq using + operator. Used in Content DSL.
 */
class SeqBuilder[T](items: Seq[T]) {
  def this(item: T) = this(Seq(item))

  def append(item: T) = new SeqBuilder[T](items.appended(item))

  def append(builder: SeqBuilder[T]) =
    new SeqBuilder[T](items.appendedAll(builder.toSeq))

  def append(toAppend: IterableOnce[T]) =
    new SeqBuilder[T](items.appendedAll(toAppend))

  def +(item: T): SeqBuilder[T] = append(item)

  def +(builder: SeqBuilder[T]): SeqBuilder[T] = append(builder)

  def +(toAppend: IterableOnce[T]): SeqBuilder[T] = append(toAppend)

  def unary_+ : SeqBuilder[T] = this

  def toSeq: Seq[T] = items
}