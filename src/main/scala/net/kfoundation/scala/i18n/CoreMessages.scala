package net.kfoundation.scala.i18n

import net.kfoundation.scala.i18n.Dictionary.{Key, Scope, Value}

object CoreMessages {

  object IO extends Scope("net.kfoundation.scala.io", Set()) {
    val PATH_SEGMENT_HAS_SLASH = new Key("MESSAGE001", Set(
      new Value(Dialect.EN_US, "Path segment should not contain path separator character. Was: %s")))
  }

}
