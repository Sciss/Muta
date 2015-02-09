/*
 *  DocumentFrame.scala
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

import de.sciss.treetable.TreeTable
import de.sciss.file.File
import scala.concurrent.Future
import scala.swing.{Action, SequentialContainer}
import de.sciss.desktop.Window

import scala.util.Try

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

  def genome: system.GenomeSel

  def mainTable     : TreeTable[_, _]
  def breedingTable : TreeTable[_, _]

  def open(): Unit

  def file: Option[File]

  def load(file: File, quiet: Boolean = false): Try[Unit]
  def save(file: File, quiet: Boolean = false): Try[Unit]

  def iterate(n: Int, quiet: Boolean = false): Future[Unit]

  type Node <: DocumentFrame.NodeLike[system.Chromosome]

  def selectedNodes: Vec[Node]
  var currentTable : Vec[Node]

  def topPanel: SequentialContainer

  def window: Window

  def bindMenu(path: String, action: Action): Unit
}