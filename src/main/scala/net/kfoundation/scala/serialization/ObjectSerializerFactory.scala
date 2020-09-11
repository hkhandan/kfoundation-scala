package net.kfoundation.scala.serialization

import java.io.OutputStream


trait ObjectSerializerFactory {
  def of(output: OutputStream, indentSize: Int, compact: Boolean): ObjectSerializer
  def of(output: OutputStream): ObjectSerializer
  def of(output: OutputStream, indentSize: Int): ObjectSerializer =
    of(output, indentSize, false)
}