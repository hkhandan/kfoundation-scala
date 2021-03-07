// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.logging

import net.kfoundation.scala.UString

import java.io.{ByteArrayOutputStream, PrintWriter}
import java.text.SimpleDateFormat
import java.util.{Date, logging => j}


object JavaLoggerFactory {

  private class HandlerImpl extends j.Handler {
    private val f = new SimpleDateFormat("HH:mm:ss.S")

    private def throwableToString(th: Throwable): String = {
      val os = new ByteArrayOutputStream
      val wr = new PrintWriter(os)
      th.printStackTrace(wr)
      UString.of(os.toByteArray).toString()
    }

    override def publish(record: j.LogRecord): Unit = {
      val sb = new StringBuilder
      sb.append("[").append(record.getLevel).append("] ")
        .append(f.format(new Date(record.getMillis))).append(" ")
        .append(record.getMessage)
      if(record.getThrown != null) {
        sb.append("\n")
          .append(throwableToString(record.getThrown))
      }
      System.out.println(sb.toString)
    }

    override def flush(): Unit = {
    }

    @throws[SecurityException]
    override def close(): Unit = {
    }
  }


  private class LoggerImpl(impl: j.Logger) extends Logger {
    override def log(level: Level, message: () => UString, th: Option[Throwable]): Unit =
      log(level, message().toString(), th)

    override def log(level: Level, message: UString, th: Option[Throwable]): Unit =
      th match {
        case Some(t) => impl.log(toJava(level), message.toString(), t)
        case None => impl.log(toJava(level), message.toString())
      }
  }


  private def toJava(l: Level): j.Level = l match {
    case Level.DEBUG => j.Level.FINE
    case Level.INFO => j.Level.INFO
    case Level.WARN => j.Level.WARNING
    case Level.ERROR => j.Level.SEVERE
    case _ => j.Level.FINE
  }


  j.LogManager.getLogManager.reset()
  j.LogManager.getLogManager.getLogger("").addHandler(new HandlerImpl())
}


class JavaLoggerFactory extends LoggerFactory {
  override def newLogger(cls: Class[_]): Logger =
    new JavaLoggerFactory.LoggerImpl(
      j.Logger.getLogger(cls.getCanonicalName))
}