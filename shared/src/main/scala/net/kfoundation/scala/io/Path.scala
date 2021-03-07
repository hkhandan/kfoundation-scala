// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.io

import net.kfoundation.scala.parse.lex.{CodeWalker, IntegralToken}
import net.kfoundation.scala.parse.{ParseError, Parser}
import net.kfoundation.scala.{UChar, UString}

import java.io._
import java.net.{URI, URLDecoder}
import java.nio.charset.StandardCharsets
import java.nio.file.{FileSystemNotFoundException, FileSystems, Paths}



object Path {
  private val DOT: UChar = '.'
  private val SLASH: UChar = '/'
  private val SLASH_CODE: Int = '/'
  private val PERCENT_CODE: Char = '%'

  val ROOT = new Path(false, Seq.empty)
  val EMPTY = new Path(true, Seq.empty)

  val PARSER: Parser[Path] = parser(_ => true)

  /** Creates a path parser that uses the given validator for path segments */
  def parser(isValid: Int => Boolean): Parser[Path] = new Parser[Path] {
    import ParseError._

    private def tryReadSegment(w: CodeWalker): Option[UString] = {
      val buffer = new ByteArrayOutputStream()
      var hasMore = w.hasMore
      while(hasMore) {
        val ch = w.tryRead(ch => ch != '/' && isValid(ch))
        if(ch == -1) {
          if(w.tryRead(PERCENT_CODE)) {
            val d1 = w.tryRead(IntegralToken.isValidHexDigit(_))
            val d2 = w.tryRead(IntegralToken.isValidHexDigit(_))
            if(d1 == -1 || d2 == -1) {
              w.parseError(MISSING_TOKEN, SHOULD -> "TWO_HEX_DIGITS")
            }
            buffer.write(IntegralToken.hexByte(d1, d2))
          } else {
            hasMore = false
          }
        } else {
          buffer.write(UChar.encodeUtf8(ch))
        }
      }

      if(buffer.size == 0) {
        None
      } else {
        w.commit()
        Some(UString.of(buffer.toByteArray))
      }
    }

    override def tryRead(w: CodeWalker): Option[Path] = {
      val isAbsolute = w.tryRead(SLASH_CODE)
      if(isAbsolute) {
        w.commit()
      }
      var segments: Seq[UString] = Nil
      var hasMore = w.hasMore
      while(hasMore) {
        tryReadSegment(w).foreach(s => segments = segments :+ s)
        hasMore = w.tryRead(SLASH_CODE)
        w.commit()
      }
      Some(if(segments.isEmpty) {
        if(isAbsolute) ROOT else EMPTY
      } else {
        new Path(!isAbsolute, segments)
      })
    }
  }

  /** Parses the given string into a Path object */
  def apply(str: UString): Path = {
    val walker = CodeWalker.of(str)
    val path = PARSER.tryRead(walker)
    if(walker.hasMore) {
      throw walker.parseError(ParseError.BAD_INPUT, ParseError.WAS -> str)
    }
    path.get
  }

  /** Path of the Java resource using the ClassLoader of the given class. */
  def ofResource(cls: Class[_], path: String): Path = {
    import scala.jdk.CollectionConverters.MapHasAsJava

    val url = cls.getResource(path)
    if (url == null) {
      throw new IOException("Resource not found: " + path)
    }
    val uri = url.toURI
    if (!(uri.getScheme == "jar")) {
      apply(uri.getPath)
    } else {
      val parts = uri.toString.split("!")
      val jarUri = URI.create(parts(0))

      val fs = try {
        FileSystems.getFileSystem(jarUri)
      } catch {
        case _: FileSystemNotFoundException =>
          FileSystems.newFileSystem(jarUri, Map("create" -> "true").asJava)
      }

      val p = fs.getPath(URLDecoder.decode(parts(1), StandardCharsets.UTF_8.name))
      apply(p.toString)
    }
  }

  /** Constructs an absolute path with the given segments */
  def absolute(segments: UString*): Path = new Path(false, segments)

  /** Constructs a relative path with the given segments */
  def relative(segments: UString*): Path = new Path(true, segments)
}



/**
 * Unified abstraction and interface to work with file system. This class is
 * used throughout the KFoundation in place of Java's File and Path classes.
 */
class Path private (
  val isRelative: Boolean,
  val segments: Seq[UString])
{
  import Path._

  /**
   * Returns the last segment of the path.
   */
  def fileName: Option[UString] = segments.lastOption


  /**
   * Returns the substring after period '.' at the end of the path.
   */
  def extension: Option[UString] =
    fileName.flatMap(n => {
      val parts = n.split(DOT)
      if(parts.length > 1) {
        Some(parts.last)
      } else {
        None
      }
    })


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
  def parent = new Path(isRelative,
    if(segments.isEmpty) segments else segments.dropRight(1))


  /**
   * Opens and returns an InputStream to read from the file pointed to by this path.
   */
  def newInputStream: FileInputStream = new FileInputStream(toString)


  /**
   * Opens and returns an output stream to write to the file pointed to by this path.
   */
  def newOutputStream: FileOutputStream = new FileOutputStream(toString)


  /**
   * Produces a FileReader to read from the file pointed to by this path.
   */
  def newReader: FileReader = new FileReader(toString)


  /**
   * Produces a FileWriter to write to the file pointed to by this path.
   */
  def newWriter: FileWriter = new FileWriter(toString)


  /**
   * Adds a segment to this path, meant to point a file or directory under
   * this path. The input segment cannot contain a path separator '/', or else
   * IllegalArgumentException will be thrown.
   * @throws IllegalArgumentException when in put contains a path separator.
   */
  def append(segment: UString): Path = if(segment.contains(SLASH))
    throw new IllegalArgumentException("Path segment should not contain path separator. Given: " + segment)
  else
    new Path(isRelative, segments :+ segment)

  /** Appends the segments of the given path to the end of this path. */
  def append(path: Path): Path =
    new Path(isRelative, segments.appendedAll(path.segments))


  /** Appends a segment to this path. */
  def /(segment: UString): Path = append(segment)


  /** Appends the segments of the given path to the end of this path. */
  def /(path: Path): Path = append(path)


  /** Produces a new Path which is the same as this Path, except it is absolute. */
  def toAbsolute: Path = new Path(false, segments)


  def toUString: UString = {
    val builder = UString.builder
    if(!isRelative) {
      builder.append(SLASH)
    }
    builder.appendJoining(segments, SLASH)
      .build
  }


  /**
   * Returns string representation of this path.
   */
  override def toString: String = toUString


  override def equals(other: Any): Boolean = other match {
    case that: Path =>
      isRelative == that.isRelative &&
      segments == that.segments
    case _ => false
  }


  override def hashCode(): Int = {
    val state = Seq(isRelative, segments)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}