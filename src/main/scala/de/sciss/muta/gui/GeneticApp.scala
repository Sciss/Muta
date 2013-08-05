package de.sciss.muta
package gui

import de.sciss.desktop.impl.SwingApplicationImpl
import de.sciss.desktop.{KeyStrokes, Menu}
import java.awt.event.KeyEvent

abstract class GeneticApp[S <: Sys](val system: S) extends SwingApplicationImpl("Genetic Algorithm") {
  app =>

  type Document = Unit // gui.Document

  // protected def newDocument(): Document

  protected lazy val menuFactory = {
    import Menu._
    import KeyStrokes._
    import KeyEvent._
    Root().add(
      Group("file", "File").add(
        Item("new")("New" -> (menu1 + VK_N)) {
          // val doc = newDocument() // new Document
          new DocumentFrame(app) // (doc)
        }
      ).add(
        Group("import", "Import").add(
          Item("settings", proxy("Algorithm Settings..."      -> (menu1 + alt   + VK_O)))
        )
      ).add(
        Group("export", "Export")
          //        .add(
          //          Item("lily", proxy("Selection As Lilypond Score..." -> (menu1 + shift + VK_S)))
          //        )
          .add(
          Item("settings", proxy("Algorithm Settings..."      -> (menu1 + alt   + VK_S)))
        )
        .add(
          Item("table", proxy("Selection As PDF Table..."     -> (menu1 +         VK_T)))
        )
      )
    )
  }
}