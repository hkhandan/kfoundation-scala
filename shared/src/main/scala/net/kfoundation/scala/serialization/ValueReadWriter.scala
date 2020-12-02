// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.serialization


/**
 * Implementation of this trait can function both as reader and writer of
 * values of type T.
 * @tparam T type of values to be read or write.
 */
trait ValueReadWriter[T] extends ValueReader[T] with ValueWriter[T] {

  /**
   * With bi-directional mapping to and from type S provided, this
   * method produces a ValueReadWriter that can read values of type S.
   */
  def mapRW[S](implicit toConversion: T => S, fromConversion: S => T):
  ValueReadWriter[S] =
    new ValueReadWriter[S] {
      override def write(serializer: ObjectSerializer, value: S): Unit =
        ValueReadWriter.this.write(serializer, fromConversion(value))
      override def read(deserializer: ObjectDeserializer): S =
        toConversion(ValueReadWriter.this.read(deserializer))
    }


  /**
   * Produces a ValueReadWriter that can read or write sequence of T.
   */
  def seq: ValueReadWriter[Seq[T]] = new ValueReadWriter[Seq[T]] {
    override def write(serializer: ObjectSerializer, value: Seq[T]): Unit = {
      serializer.writeCollectionBegin()
      value.foreach(item => ValueReadWriter.this.write(serializer, item))
      serializer.writeCollectionEnd()
    }

    override def read(deserializer: ObjectDeserializer): Seq[T] = {
      var result = Seq[T]()
      deserializer.readCollectionBegin()
      while(!deserializer.tryReadCollectionEnd()) {
        result = result :+ ValueReadWriter.this.read(deserializer)
      }
      result
    }
  }

}