package de.sciss.muta

import scala.reflect.ClassTag
import reflect.runtime.{universe => ru}

trait Sys[S <: Sys[S]] {
  type Chromosome
  type Genome     = Vec[Chromosome]
  type GenomeVal  = Vec[(Chromosome, Double)]
  type GenomeSel  = Vec[(Chromosome, Double, Boolean)]

  type Global

  def rng(seed: Long = 0L): util.Random = new util.Random(seed)

  def defaultGeneration: Generation[S]
  def defaultEvaluation: Evaluation[S]
  def defaultSelection : Selection [S]
  def defaultBreeding  : Breeding  [S]

  implicit def chromosomeClassTag: ClassTag[S#Chromosome]
  // implicit def globalTypeTag: ru.TypeTag[S#Global]
  implicit def selfTypeTag: ru.TypeTag[S]
}