/*
 *  SettingsFrame.scala
 *  (Muta)
 *
 *  Copyright (c) 2013-2014 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v3+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.muta
package gui

import de.sciss.guiflitz.AutoView
import de.sciss.desktop.Window
import de.sciss.desktop.impl.WindowImpl
import scala.swing.{Action, ScrollPane}

class SettingsFrame[A](app: GeneticApp[_], view: AutoView[A], title: String) { me =>
  final def value       : A        = view.cell()
  final def value_=(eval: A): Unit = view.cell() = eval

  val window: Window = new WindowImpl {
    def handler     = app.windowHandler
    title           = s"${me.title} Settings"
    closeOperation  = Window.CloseDispose
    contents        = new ScrollPane(view.component)

    bindMenu("window.pack", Action(null) {
      view.component.revalidate()
      pack()
    })
    pack()
    front()
  }
}