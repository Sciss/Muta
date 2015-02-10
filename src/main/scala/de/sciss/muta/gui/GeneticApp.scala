/*
 *  GeneticApp.scala
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

import javax.swing.UIManager

import de.sciss.desktop.impl.{LogWindowImpl, SwingApplicationImpl, WindowHandlerImpl}
import de.sciss.desktop.{Desktop, FileDialog, KeyStrokes, Menu, WindowHandler}
import de.sciss.file.File

import scala.swing.event.Key
import scala.util.Try

/** The stub to create a genetic algorithm swing application. Usually you will have an object
  * extending this class, which is then the main swing entry point.
  */
abstract class GeneticApp[S <: System](val system: S, name: String = "Genetic Algorithm")
  extends SwingApplicationImpl(name) {

  app =>

  type Document = Unit // gui.Document

  protected def useNimbus         = Desktop.isLinux
  protected def useInternalFrames = !Desktop.isMac
  protected def useLogWindow      = true

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
    if (useLogWindow) new LogWindowImpl {
      def handler: WindowHandler = app.windowHandler
    }
  }

  /** Override this to enforce a specific row height in the genome tables. The default value of `-1`
    * indicates that there is no preferred row height.
    */
  protected def rowHeight = -1

  final protected def makeDocumentFrame(): DocumentFrame[S] = {
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
  
  final protected def openDocument(file: File): Try[DocumentFrame[S]] = {
    val fr = makeDocumentFrame()
    fr.load(file).map { _ =>
      fr.open()
      fr
    }
  }

  protected lazy val menuFactory = {
    import KeyStrokes._
    import Menu._
    val itQuit = Item.Quit(app)
    val mFile = Group("file", "File").add(
        Item("new")("New" -> (menu1 + Key.N)) {
          val fr = makeDocumentFrame()
          fr.open()
        }
      ).add(
        Item("open")("Open..." -> (menu1 + Key.O)) {
          val dlg = FileDialog.open(title = "Open Document")
          dlg.show(None).foreach { file =>
            openDocument(file)
          }
        }
      ).addLine()
      .add(
        Group("import", "Import").add(
          Item("settings", proxy("Algorithm Settings..." -> (menu1 + alt + Key.O)))
        )
      ).add(
        Group("export", "Export")
          //        .add(
          //          Item("lily", proxy("Selection As Lilypond Score..." -> (menu1 + shift + VK_S)))
          //        )
          .add(
          Item("settings", proxy("Algorithm Settings..." -> (menu1 + alt + Key.S)))
        )
        .add(
          Item("table", proxy("Selection As PDF Table..." -> (menu1 + Key.T)))
        )
      ).addLine().add(
        Item("save", proxy("Save" -> (menu1 + Key.S)))
      ).add(
        Item("save-as", proxy("Save As..." -> (menu1 + shift + Key.S)))
      )

    if (itQuit.visible) mFile.addLine().add(itQuit)
    Root().add(mFile).add(
      Group("window", "Window").add(
        Item("pack", proxy("Pack" -> (menu1 + Key.P)))
      )
    )
  }
}