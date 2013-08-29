package de.sciss.muta
package gui

import de.sciss.guiflitz.AutoView
import de.sciss.desktop.Window
import de.sciss.desktop.impl.WindowImpl
import scala.swing.ScrollPane

class SettingsFrame[A](app: GeneticApp[_], view: AutoView[A], title: String) { me =>
  final def value       : A        = view.cell()
  final def value_=(eval: A): Unit = view.cell() = eval

  val window: Window = new WindowImpl {
    def handler     = app.windowHandler
    def style       = Window.Regular
    title           = s"${me.title} Settings"
    closeOperation  = Window.CloseDispose
    contents        = new ScrollPane(view.component)
    pack()
    front()
  }
}