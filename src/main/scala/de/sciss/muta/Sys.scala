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

  def generationView(config: AutoView.Config): AutoView[Generation]

  // def randomChromosome(global: Global)(implicit random: util.Random): Chromosome

  def chromosomeView(c: Chromosome, default: swing.Label, selected: Boolean, focused: Boolean): swing.Component =
    default
}