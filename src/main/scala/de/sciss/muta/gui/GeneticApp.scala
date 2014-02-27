/*
 *  GeneticApp.scala
 *  (Muta)
 *
 *  Copyright (c) 2013-2014 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU General Public License v2+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.muta
package gui

import de.sciss.desktop.impl.{LogWindowImpl, WindowHandlerImpl, SwingApplicationImpl}
import de.sciss.desktop.{Desktop, WindowHandler, FileDialog, KeyStrokes, Menu}
import java.awt.event.KeyEvent
import javax.swing.UIManager

/** The stub to create a genetic algorithm swing application. Usually you will have an object
  * extending this class, which is then the main swing entry point.
  */
abstract class GeneticApp[S <: System](val system: S) extends SwingApplicationImpl("Genetic Algorithm") {
  app =>

  type Document = Unit // gui.Document

  protected def useNimbus         = Desktop.isLinux
  protected def useInternalFrames = !Desktop.isMac

  // protected def newDocument(): Document

  /** Override this if you wish to be informed about document frames opening. */
  protected def configureDocumentFrame(frame: DocumentFrame[S]) = ()

  override lazy val windowHandler: WindowHandler = new WindowHandlerImpl(this, menuFactory) {
    override def usesInternalFrames = app.useInternalFrames
  }

  override protected def init() = {
    val nimbusOption = if (!useNimbus) None else UIManager.getInstalledLookAndFeels.collectFirst {
      case info if info.getName == "Nimbus" => info.getClassName
    }
    nimbusOption.foreach(UIManager.setLookAndFeel)
    new LogWindowImpl {
      def handler: WindowHandler = app.windowHandler
    }
  }

  /** Override this to enforce a specific row height in the genome tables. The default value of `-1`
    * indicates that there is no preferred row height.
    */
  protected def rowHeight = -1

  private def mkDocFrame(): DocumentFrame[S] = {
    // val doc = newDocument() // new Document
    val f = DocumentFrame(app) // (doc)
    configureDocumentFrame(f)
    val rh = rowHeight
    if (rh > 0) {
      f.mainTable    .peer.setRowHeight(rh)
      f.breedingTable.peer.setRowHeight(rh)
    }
    f
  }

  protected lazy val menuFactory = {
    import Menu._
    import KeyStrokes._
    import KeyEvent._
    Root().add(
      Group("file", "File").add(
        Item("new")("New" -> (menu1 + VK_N)) {
          val fr = mkDocFrame()
          fr.open()
        }
      ).add(
        Item("open")("Open..." -> (menu1 + VK_O)) {
          val dlg = FileDialog.open(title = "Open Document")
          dlg.show(None).foreach { file =>
            val fr = mkDocFrame()
            fr.load(file)
            fr.open()
          }
        }
      ).addLine()
      .add(
        Group("import", "Import").add(
          Item("settings", proxy("Algorithm Settings..." -> (menu1 + alt + VK_O)))
        )
      ).add(
        Group("export", "Export")
          //        .add(
          //          Item("lily", proxy("Selection As Lilypond Score..." -> (menu1 + shift + VK_S)))
          //        )
          .add(
          Item("settings", proxy("Algorithm Settings..." -> (menu1 + alt + VK_S)))
        )
        .add(
          Item("table", proxy("Selection As PDF Table..." -> (menu1 + VK_T)))
        )
      ).addLine().add(
        Item("save", proxy("Save" -> (menu1 + VK_S)))
      ).add(
        Item("save-as", proxy("Save As..." -> (menu1 + shift + VK_S)))
      )
    ).add(
      Group("window", "Window").add(
        Item("pack", proxy("Pack" -> (menu1 + VK_P)))
      )
    )
  }
}