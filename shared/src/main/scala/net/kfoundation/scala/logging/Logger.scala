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

import java.io.{PrintWriter, StringWriter}



/**
 * Logger factory.
 */
object Logger {
  private val FACTORY = new JavaLoggerFactory()


  def toString(th: Throwable): String = {
    val sw = new StringWriter()
    val pw = new PrintWriter(sw)
    th.printStackTrace(pw)
    sw.toString
  }


  /** Creates a logger for the given class. */
  def apply(cls: Class[_]): Logger = FACTORY.newLogger(cls)


  /** Creates a logger for the class of the given object. */
  def apply(obj: AnyRef): Logger = FACTORY.newLogger(obj.getClass)
}



/**
 * A portable logging utility. The underlying implementation may differ
 * depending on the target platform.
 * Lambda variant for each method is provided to help with performance
 * optimization.
 */
abstract class Logger {
  def log(level: Level, message: () => UString, th: Option[Throwable]): Unit
  def log(level: Level, message: UString, th: Option[Throwable]): Unit

  def debug(message: UString): Unit = log(Level.DEBUG, message, None)
  def info(message: UString): Unit = log(Level.INFO, message, None)
  def info(message: () => UString): Unit = log(Level.INFO, message, None)
  def warn(message: UString): Unit = log(Level.WARN, message, None)
  def warn(message: () => UString): Unit = log(Level.WARN, message, None)
  def error(message: UString): Unit = log(Level.ERROR, message, None)
  def error(message: () => UString): Unit = log(Level.ERROR, message, None)
  def error(th: Throwable): Unit = log(Level.ERROR, th.getMessage, Some(th))

  def error(message: UString, th: Throwable): Unit =
    log(Level.ERROR, message, Some(th))

  def error(message: () => UString, th: Throwable): Unit =
    log(Level.ERROR, message, Some(th))
}