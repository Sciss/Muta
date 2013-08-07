package de.sciss.muta
package gui

import de.sciss.treetable.TreeTable

object DocumentFrame {
  def apply[S <: System](app: GeneticApp[S]): DocumentFrame[S] = new impl.DocumentFrameImpl[S](app)
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
}