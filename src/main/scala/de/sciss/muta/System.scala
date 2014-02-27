/*
 *  System.scala
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

import scala.reflect.ClassTag
import de.sciss.guiflitz.AutoView
import play.api.libs.json.Format
import de.sciss.muta

trait System {
  /** The chromosome is one "sequence" to be generated and evaluated. Typically this will be a collection
    * type such as `Vector` */
  type Chromosome
  /** The genome is the pool of all chromosomes at one iteration. */
  type Genome     = Vec[Chromosome]
  /** An evaluated genome is a genome with a fitness for each chromosome. */
  type GenomeVal  = Vec[(Chromosome, Double)]
  /** A selected genome is an evaluated genome with a selection flag for each chromosome. */
  type GenomeSel  = Vec[(Chromosome, Double, Boolean)]

  type Evaluation <: muta.Evaluation[Chromosome]

  def defaultEvaluation: Evaluation

  /** When using manual evaluation, the number of steps for choosing fitness. Otherwise zero. */
  def humanEvaluationSteps: Int = 0

  final def hasHumanEvaluation: Boolean = humanEvaluationSteps > 0

  def hasHumanSelection: Boolean = false

  /** This type defines the global parameters of the genetic system. They can be used
    * for generating chromosomes (e.g., specifying a chromosome length) in the original
    * population or breeding.
    */
  type Global

  /** Creates a new random number generator with a given seed. */
  def rng(seed: Long = 0L): util.Random = new util.Random(seed)

  type Generation <: muta.Generation[Chromosome, Global]
  type Selection  <: muta.Selection [Chromosome]
  type Breeding   <: muta.Breeding  [Chromosome, Global]

  def defaultGeneration: Generation
  def defaultSelection : Selection
  def defaultBreeding  : Breeding

  implicit def chromosomeClassTag: ClassTag[Chromosome]
  // implicit def globalTypeTag: ru.TypeTag[S#Global]
  // implicit def selfTypeTag: ru.TypeTag[S]

  // implicit def generationTypeTag: ru.TypeTag[Generation]

  // ---- these should disappear; they are needed because of limitations of AutoView's type resolution ----

  /** Creates a GUI view for editing the generation settings. */
  def generationView(init: Generation, config: AutoView.Config): AutoView[Generation]
  /** Creates a GUI view for editing the selection settings. */
  def selectionView (init: Selection , config: AutoView.Config): AutoView[Selection ]
  /** Creates a GUI view for editing the breeding settings. */
  def breedingView  (init: Breeding  , config: AutoView.Config): AutoView[Breeding  ]

  /** Creates a GUI view for editing the evaluation settings. */
  def evaluationViewOption: Option[(Evaluation, AutoView.Config) => AutoView[Evaluation]]

  def generationFormat: Format[Generation]
  def selectionFormat : Format[Selection ]
  def breedingFormat  : Format[Breeding  ]
  def evaluationFormat: Format[Evaluation]
  def chromosomeFormat: Format[Chromosome]

  /** Provides a view component for the chromosomes in the genome table.
    * This method is guaranteed single threaded and called on the event dispatch thread,
    * thus a single swing components can be reused.
    *
    * @param  c       the chromosome to view
    * @param  default the default table view, already configured according to selection, focus, and chromosome
    *                 `toString` representation. If this type (`Label`) is suitable, the method can just customize
    *                 the label and return it straight away
    * @param  selected  `true` if the chromosome is visually selected in the table
    * @param  focused   `true` if the table cell corresponding to the chromosome is currently focused
    * @return the swing component that views/paints the chromosome
    */
  def chromosomeView(c: Chromosome, default: swing.Label, selected: Boolean, focused: Boolean): swing.Component =
    default

  def chromosomeEditorOption: Option[(swing.Component, () => Chromosome, Chromosome => Unit)] = None

  final def hasChromosomeEditor: Boolean = chromosomeEditorOption.isDefined
}