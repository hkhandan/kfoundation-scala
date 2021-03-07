// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.uui

import net.kfoundation.scala.UString
import net.kfoundation.scala.io.URL


object MediaTraits {
  trait Clipboard {
    def copy(text: UString): Unit
  }
  trait Navigator {
    def forward(url: URL): Unit
    def open(url: URL): Unit
  }
}


class MediaTraits(val isPaged: Boolean, val isInteractive: Boolean,
  val graphics: GraphicsMode, val colors: ColorMode,
  val clipboard: Option[MediaTraits.Clipboard],
  val navigator: Option[MediaTraits.Navigator],
  val url: URL)