package net.kfoundation.scala.i18n

import net.kfoundation.scala.util.WQName
import org.scalatest.funsuite.AnyFunSuite



object LocalizerTest {
  import MultiDictionary._

  val FR: Dialect = new Language("fr", "fre").asDialect

  val dictionary = new MultiDictionary(Dialect.EN_US, Seq.empty, Seq(
    new DomainSet(WQName("test.domain"), Seq(
      new EntrySet("TIME_SHORT", Seq(
        new Entry(Dialect.EN_US, "EST|hh:mm a"),
        new Entry(FR, "CET|HH:mm")
      )),
      new EntrySet("TIME_LONG", Seq(
        new Entry(Dialect.EN_US, "hh:mm:ss a"),
        new Entry(FR, "HH:mm:ss.SSS")
      )),
      new EntrySet("DATE_TIME_LONG", Seq(
        new Entry(Dialect.EN_US, "CHINESE|CET|YYYY mmm dddd hh:mm a")
      ))
    ))
  ))
}



class LocalizerTest extends AnyFunSuite {

}
