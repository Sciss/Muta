package de.sciss.muta
package gui

import de.sciss.treetable.TreeTable
import de.sciss.file.File
import scala.swing.{Action, SequentialContainer, Panel}
import de.sciss.desktop.Window

object DocumentFrame {
  def apply[S <: System](app: GeneticApp[S]): DocumentFrame[S] = new impl.DocumentFrameImpl[S](app)

  trait NodeLike[C] {
    def index     : Int
    def chromosome: C
    def fitness   : Double
    def selected  : Boolean
  }
}
trait DocumentFrame[S <: System] {
  val application: GeneticApp[S]
  val system: S

  var evaluation: system.Evaluation
  var selection : system.Selection
  var breeding  : system.Breeding
  var generation: system.Generation
  var info      : HeaderInfo
  var iterations: Int

  def mainTable     : TreeTable[_, _]
  def breedingTable : TreeTable[_, _]

  def open(): Unit

  def load(file: File): Unit

  type Node <: DocumentFrame.NodeLike[system.Chromosome]

  def selectedNodes: Vec[Node]

  def topPanel: SequentialContainer

  def window: Window

  def bindMenu(path: String, action: Action): Unit
}