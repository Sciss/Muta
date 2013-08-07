package de.sciss.muta
package gui

import de.sciss.desktop.impl.SwingApplicationImpl
import de.sciss.desktop.{KeyStrokes, Menu}
import java.awt.event.KeyEvent

/** The stub to create a genetic algorithm swing application. Usually you will have an object
  * extending this class, which is then the main swing entry point.
  */
abstract class GeneticApp[S <: System](val system: S) extends SwingApplicationImpl("Genetic Algorithm") {
  app =>

  type Document = Unit // gui.Document

  // protected def newDocument(): Document

  /** Override this if you wish to be informed about document frames opening. */
  protected def configureDocumentFrame(frame: DocumentFrame[S]) = ()

  /** Override this to enforce a specific row height in the genome tables. The default value of `-1`
    * indicates that there is no preferred row height.
    */
  protected def rowHeight = -1

  protected lazy val menuFactory = {
    import Menu._
    import KeyStrokes._
    import KeyEvent._
    Root().add(
      Group("file", "File").add(
        Item("new")("New" -> (menu1 + VK_N)) {
          // val doc = newDocument() // new Document
          val f = DocumentFrame(app) // (doc)
          configureDocumentFrame(f)
          val rh = rowHeight
          if (rh > 0) {
            f.mainTable    .peer.setRowHeight(rh)
            f.breedingTable.peer.setRowHeight(rh)
          }
          f.open()
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