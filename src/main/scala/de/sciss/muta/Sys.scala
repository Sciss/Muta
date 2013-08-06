package de.sciss
package muta

import scala.reflect.ClassTag
import reflect.runtime.{universe => ru}
import de.sciss.guiflitz.AutoView

trait Sys {
  type S <: Sys

  type Chromosome
  type Genome     = Vec[Chromosome]
  type GenomeVal  = Vec[(Chromosome, Double)]
  type GenomeSel  = Vec[(Chromosome, Double, Boolean)]

  type Global

  def rng(seed: Long = 0L): util.Random = new util.Random(seed)

  type Generation <: muta.Generation[Chromosome, Global]
  type Evaluation <: muta.Evaluation[Chromosome]
  type Selection  <: muta.Selection [Chromosome]
  type Breeding   <: muta.Breeding  [Chromosome, Global]

  def defaultGeneration: Generation
  def defaultEvaluation: Evaluation
  def defaultSelection : Selection
  def defaultBreeding  : Breeding

  implicit def chromosomeClassTag: ClassTag[Chromosome]
  // implicit def globalTypeTag: ru.TypeTag[S#Global]
  // implicit def selfTypeTag: ru.TypeTag[S]

  // implicit def generationTypeTag: ru.TypeTag[Generation]

  // ---- these should disappear; they are needed because of limitations of AutoView's type resolution ----

  def generationView(config: AutoView.Config): AutoView[Generation]
  def evaluationView(config: AutoView.Config): AutoView[Evaluation]
  def selectionView (config: AutoView.Config): AutoView[Selection ]
  def breedingView  (config: AutoView.Config): AutoView[Breeding  ]

  // def randomChromosome(global: Global)(implicit random: util.Random): Chromosome

  def chromosomeView(c: Chromosome, default: swing.Label, selected: Boolean, focused: Boolean): swing.Component =
    default
}