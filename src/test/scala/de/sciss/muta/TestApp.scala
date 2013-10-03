package de.sciss.muta

import de.sciss.muta.gui.DocumentFrame
import scala.swing.{Button, Label}

object TestApp extends gui.GeneticApp[TestSys.type](TestSys) {
  override protected def configureDocumentFrame(frame: DocumentFrame[TestSys.type]): Unit =
    frame.topPanel.contents += Button("Print") {
      frame.selectedNodes.foreach(println)
    }
}