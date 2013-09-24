package de.sciss.muta.gui

import impl.{LogWindowImpl => Impl}
import de.sciss.desktop.{SwingApplication, Window}
import java.awt.GraphicsEnvironment
import scala.swing.Swing
import Swing._

object LogWindow {
  //  val horizontalPlacement   = 1.0f
  //  val verticalPlacement     = 1.0f
  //  val placementPadding      = 20
  //
  //  lazy val instance: LogWindow  = new Impl

  private def placeWindow(w: Window, horizontal: Float, vertical: Float, padding: Int): Unit = {
    val ge  = GraphicsEnvironment.getLocalGraphicsEnvironment
    val bs  = ge.getMaximumWindowBounds
    val b   = w.size
    val x   = (horizontal * (bs.width  - padding * 2 - b.width )).toInt + bs.x + padding
    val y   = (vertical   * (bs.height - padding * 2 - b.height)).toInt + bs.y + padding
    w.location = (x, y)
  }

  def apply(app: SwingApplication, horizontalPlacement: Float = 1f, verticalPlacement: Float = 1f,
            placementPadding: Int = 20): LogWindow = {
    val res = new Impl(app)
    placeWindow(res, horizontalPlacement, verticalPlacement, placementPadding)
    res
  }
}
abstract class LogWindow extends Window {
  def log: LogPane
}