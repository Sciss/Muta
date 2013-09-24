package de.sciss.muta.gui
package impl

import swing.{Component, ScrollPane, Swing}
import java.io.OutputStream
import javax.swing.BorderFactory
import swing.event.WindowClosing
import de.sciss.desktop
import desktop.impl.WindowImpl
import desktop.Window

// lazy window - opens as soon as something goes to the console
final class LogWindowImpl(app: desktop.SwingApplication) extends LogWindow with WindowImpl {
  frame =>

//  peer.getRootPane.putClientProperty("Window.style", "small")

  def style   = Window.Auxiliary
  def handler = app.windowHandler // SwingApplication.windowHandler

  val log = {
    val cfg   = LogPane.Settings()
    cfg.rows  = 24
    LogPane(cfg)
  }

  private val observer: OutputStream = new OutputStream {
    override def write(b: Array[Byte], off: Int, len: Int): Unit = {
      log.makeDefault()               // detaches this observer
      log.outputStream.write(b, off, len)
      Swing.onEDT(frame.front())     // there we go
    }

    def write(b: Int): Unit = {
      write(Array(b.toByte), 0, 1)
    }
  }

  def observe(): Unit = {
    Console.setOut(observer)
    Console.setErr(observer)
  }

  observe()
  closeOperation = Window.CloseIgnore
  reactions += {
    case WindowClosing(_) =>
      frame.visible = false
      observe()
  }

  contents = new ScrollPane {
    contents  = Component.wrap(log.component)
    border    = BorderFactory.createEmptyBorder()
  }

  title   = "Log"
  pack()
  // import LogWindow._
  // GUI.placeWindow(frame, horizontal = horizontalPlacement, vertical = verticalPlacement, padding = placementPadding)
}