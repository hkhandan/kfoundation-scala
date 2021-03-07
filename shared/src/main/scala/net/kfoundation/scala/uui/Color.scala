// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.uui


object Color {
  /**
   * RGBA-encoded color. Each color coordinate can be between [0..1].
   */
  class RgbaColor(val red: Double, val green: Double, val blue: Double,
    val alpha: Double) extends Color
  {
    def encode32bit: (Int, Int, Int, Int) =
      (aToB(red), aToB(green), aToB(blue), aToB(alpha))

    def withAlpha(a: Double): RgbaColor = new RgbaColor(red, green, blue, a)

    def with8BitAlpha(a: Int): RgbaColor =
      new RgbaColor(red, green, blue, bToA(a))

    def toHexString: String = str(aToB(red)) + str(aToB(green)) +
      str(aToB(blue)) + str(aToB(alpha))

    override def asRgb: RgbColor = new RgbColor(red, green, blue)
    override def asRgba: RgbaColor = this

    override def asCmyk: CmykColor = {
      val black = 1 - Math.max(red, Math.max(green, blue))
      val iBlack = 1 - black
      new CmykColor((1 - red - black)/iBlack,
        (1 - green - black)/iBlack,
        (1 - blue - black)/iBlack,
        black)
    }

    override def asGrayScale: GrayScaleColor =
      new GrayScaleColor(rgbToGray(red, green, blue))

    override def asMonochrome: MonochromeColor =
      grayToMonochrome(rgbToGray(red, green, blue))

    override def toString: String = f"rgba($red%.2f, $green%.2f, $blue%.2f, $alpha%.2f)"
  }


  /**
   * RGB-encoded color. Each color coordinate can be between [0..1].
   */
  class RgbColor(red: Double, green: Double, blue: Double) extends
    RgbaColor(red, green, blue, 1)
  {
    def encode24Bit: (Int, Int, Int) = (aToB(red), aToB(green), aToB(blue))

    override def asRgb: RgbColor = this

    override def toHexString: String = str(aToB(red)) +
      str(aToB(green)) + str(aToB(blue))

    override def toString: String = s"rgb($red, $green, $blue)"
  }


  /**
   * CMYK-encoded color. Each color coordinate can be between [0..1].
   */
  class CmykColor(val cyan: Double, val magenta: Double, yellow: Double,
    black: Double) extends Color
  {
    def encode32Bit: (Int, Int, Int, Int) =
      (aToB(cyan), aToB(magenta), aToB(yellow), aToB(black))

    override def asRgba: RgbaColor = {
      val iBlack = 1 - black
      new RgbaColor((1 - cyan)*iBlack, (1 - magenta)*iBlack,
        (1 - yellow)*iBlack, 1)
    }

    override def asRgb: RgbColor = {
      val iBlack = 1 - black
      new RgbColor((1 - cyan)*iBlack, (1 - magenta)*iBlack, (1 - yellow)*iBlack)
    }

    override def asCmyk: CmykColor = this
    override def asGrayScale: GrayScaleColor = asRgb.asGrayScale
    override def asMonochrome: MonochromeColor = asRgb.asMonochrome
    override def toString: String = s"cmyk($cyan, $magenta, $yellow, $black)"
  }


  /** A gray-scale color encoding using a single value between [0..1] */
  class GrayScaleColor(val value: Double) extends Color {
    def encode8Bit: Int = Color.aToB(value)
    override def asRgba: RgbaColor = new RgbaColor(value, value, value, 1)
    override def asRgb: RgbColor = new RgbColor(value, value, value)
    override def asCmyk: CmykColor = new CmykColor(0, 0, 0, value)
    override def asGrayScale: GrayScaleColor = this
    override def asMonochrome: MonochromeColor =
      Color.grayToMonochrome(value)
    override def toString: String = s"gray($value)"
  }


  /** A monochrome color that can be either black or white. */
  trait MonochromeColor extends Color {
    def isWhite: Boolean
  }


  private val RGB_WHITE = new RgbColor(1, 1, 1)
  private val GRAY_WHITE = new GrayScaleColor(1)
  private val CMYK_WHITE = new CmykColor(0, 0, 0, 0)
  private val RGB_BLACK = new RgbColor(0, 0, 0)
  private val GRAY_BLACK = new GrayScaleColor(0)
  private val CMYK_BLACK = new CmykColor(0, 0, 0, 1)


  val WHITE: MonochromeColor = new MonochromeColor {
    override def isWhite: Boolean = true
    override def asRgba: RgbaColor = RGB_WHITE
    override def asRgb: RgbColor = RGB_WHITE
    override def asCmyk: CmykColor = CMYK_WHITE
    override def asGrayScale: GrayScaleColor = GRAY_WHITE
    override def asMonochrome: MonochromeColor = this
    override def toString: String = "white"
  }


  val BLACK: MonochromeColor = new MonochromeColor {
    override def isWhite: Boolean = false
    override def asRgba: RgbaColor = RGB_BLACK
    override def asRgb: RgbColor = RGB_BLACK
    override def asCmyk: CmykColor = CMYK_BLACK
    override def asGrayScale: GrayScaleColor = GRAY_BLACK
    override def asMonochrome: MonochromeColor = this
    override def toString: String = "black"
  }


  val CLEAR: Color = rgba(1, 1, 1, 0)
  val RED: RgbColor = rgb(1, 0, 0)
  val GREEN: RgbColor = rgb(0, 1, 0)
  val BLUE: RgbColor = rgb(0, 0, 1)


  private def str(i: Int): String = String.format("%02X", i)


  private def bToA(b: Int): Double = b.toDouble / 255


  private def aToB(a: Double): Int = (a*255).toInt


  private def rgbToGray(r: Double, g: Double, b: Double): Double =
    0.2126f * r + 0.7152f * g + 0.0722f * b


  private def grayToMonochrome(value: Double): MonochromeColor =
    if(value >= 0.5f)
      WHITE
    else
      BLACK


  /** Creates an RGB color. Each parameter can be between [0..1]. */
  def rgb(red: Double, green: Double, blue: Double): RgbColor =
    new RgbColor(red, green, blue)


  /**
   * Decodes an RGB color from three 8-bit values. Each parameter can be between
   * 0 and 255.
   */
  def rgb24b(red: Int, green: Int, blue: Int): RgbColor =
    new RgbColor(bToA(red), bToA(green), bToA(blue))


  /** Creates an RGBA color. Each parameter can be between [0..1]/ */
  def rgba(red: Double, green: Double, blue: Double, alpha: Double): RgbaColor =
    new RgbaColor(red, green, blue, alpha)


  /**
   * Decodes an RGBA color form four 8-bit values. Each parameter can be between
   * 0 and 255.
   */
  def rgba32b(red: Byte, green: Byte, blue: Byte, alpha: Byte): RgbaColor =
    new RgbaColor(bToA(red), bToA(green), bToA(blue), bToA(alpha))


  /**
   * Creates a CMYK color. Each parameter can be between [0..1].
   */
  def cmyk(cyan: Double, magenta: Double, yellow: Double, black: Double): CmykColor =
    new CmykColor(cyan, magenta, yellow, black)


  /**
   * Creates a gray-scale color. The parameter can be between [0..1].
   */
  def gray(value: Double): GrayScaleColor = new GrayScaleColor(value)


  /**
   * Decodes a gray color form an 8-bit value. The input can be between
   * 0 and 255.
   */
  def gray8b(value: Int): GrayScaleColor =
    new GrayScaleColor(Color.bToA(value))


  /**
   * Converts a boolean value to a monochrome color. True is WHITE and false
   * is BLACK.
   */
  def mono(isWhite: Boolean): MonochromeColor =
    if(isWhite)
      WHITE
    else
      BLACK
}


/** Unified interface for any color. */
trait Color {
  /** Converts this color to RGBA. */
  def asRgba: Color.RgbaColor

  /** Converts this color to RGB. */
  def asRgb: Color.RgbColor

  /** Converts this color to CMYK. */
  def asCmyk: Color.CmykColor

  /** Converts this color to the closest gray-scale color. */
  def asGrayScale: Color.GrayScaleColor

  /** Converts this color to the closes monochrome color. */
  def asMonochrome: Color.MonochromeColor
}