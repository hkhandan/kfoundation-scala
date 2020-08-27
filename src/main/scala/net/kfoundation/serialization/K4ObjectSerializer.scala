package net.kfoundation.serialization

import java.io.{ByteArrayOutputStream, OutputStream}

import net.kfoundation.UString

object K4ObjectSerializer {

//  def toK4String[T](value: T)(implicit writer: ValueWriter[T]): UString = {
//    val buffer = new ByteArrayOutputStream()
//    writer.write(new K4ObjectSerializer(buffer), value)
//    UString.of(buffer.toByteArray)
//  }

}

class K4ObjectSerializer(os: OutputStream) {

}
