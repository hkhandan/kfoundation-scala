// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.io

import java.io.{File, FileInputStream, FileOutputStream, FileReader, FileWriter}
import java.nio.file.Paths



object Path {
  private val DOT = "."
  private val SLASH = "/"
}



/**
 * Unified abstraction and interface to work with file system. This class is
 * used throughout the KFoundation in place of Java's File and Path classes.
 */
class Path private (
  val isRelative: Boolean,
  val segments: Seq[String])
{
  import Path._

  /**
   * Returns the last segment of the path.
   */
  def getFileName: Option[String] = segments.lastOption


  /**
   * Returns the substring after period '.' at the end of the path.
   */
  def getExtension: Option[String] = getFileName
    .filter(_.contains(DOT))
    .map(n => n.substring(n.lastIndexOf(DOT) + 1))


  /**
   * Produces a java.nio.file.Path instance equivalent of this object.
   */
  def toJavaPath: java.nio.file.Path = Paths.get(toString)


  /**
   * Produces a java.io.File equivalent instance of this object.
   */
  def toJavaFile = new File(toString)


  /**
   * Return parent of the path represented by this object.
   * That is, all the segments excluding the last one.
   */
  def getParent = new Path(isRelative,
    if(segments.isEmpty) segments else segments.dropRight(1))


  /**
   * Opens and returns an InputStream to read from the file pointed to by this path.
   */
  def getInputStream: FileInputStream = new FileInputStream(toString)


  /**
   * Opens and returns an output stream to write to the file pointed to by this path.
   */
  def getOutputStream: FileOutputStream = new FileOutputStream(toString)


  /**
   * Produces a FileReader to read from the file pointed to by this path.
   */
  def getReader: FileReader = new FileReader(toString)


  /**
   * Produces a FileWriter to write to the file pointed to by this path.
   */
  def getWriter: FileWriter = new FileWriter(toString)


  /**
   * Adds a segment to this path, meant to point a file or directory under
   * this path. The input segment cannot contain a path separator '/', or else
   * IllegalArgumentException will be thrown.
   * @throws IllegalArgumentException when in put contains a path separator.
   */
  def add(segment: String): Path = if(segment.contains(SLASH))
    throw new IllegalArgumentException("Path segment should not contain path separator. Given: " + segment)
  else
    new Path(isRelative, segments :+ segment)


  /**
   * Returns string representation of this path.
   */
  override def toString: String = (if(isRelative) "" else SLASH)
    .concat(segments.mkString(SLASH))
}