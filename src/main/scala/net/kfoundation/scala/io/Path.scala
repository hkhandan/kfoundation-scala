package net.kfoundation.scala.io

import java.io.{File, FileInputStream, FileOutputStream, FileReader, FileWriter}
import java.nio.file.Paths

object Path {
  private val DOT = "."
  private val SLASH = "/"
}


class Path private (
  val isRelative: Boolean,
  val segments: Seq[String])
{
  import Path._

  def getFileName: Option[String] = segments.lastOption

  def getExtension: Option[String] = getFileName
    .filter(_.contains(DOT))
    .map(n => n.substring(n.lastIndexOf(DOT) + 1))

  def toJavaPath: java.nio.file.Path = Paths.get(toString)
  def toJavaFile = new File(toString)

  def getParent = new Path(isRelative,
    if(segments.isEmpty) segments else segments.dropRight(1))

  def getInputStream: FileInputStream = new FileInputStream(toString)
  def getOutputStream: FileOutputStream = new FileOutputStream(toString)
  def getReader: FileReader = new FileReader(toString)
  def getWriter: FileWriter = new FileWriter(toString)

  def add(segment: String): Path = if(segment.contains(SLASH))
    throw new IllegalArgumentException("" /* TODO */)
  else
    new Path(isRelative, segments :+ segment)

  override def toString: String = (if(isRelative) "" else SLASH)
    .concat(segments.mkString(SLASH))
}