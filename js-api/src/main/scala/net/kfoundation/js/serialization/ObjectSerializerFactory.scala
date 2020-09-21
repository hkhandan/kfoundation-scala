package net.kfoundation.js.serialization

import java.io.OutputStream

import net.kfoundation.scala.serialization.ObjectSerializer


trait ObjectSerializerFactory {
  def newInstance(output: OutputStream): ObjectSerializer
}