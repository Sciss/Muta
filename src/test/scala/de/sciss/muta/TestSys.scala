package de.sciss.muta

import reflect.runtime.{universe => ru}

class TestSys extends Sys[TestSys] {
  type S          = TestSys
  type Chromosome = Vec[Boolean]
  type Global     = Unit

  def defaultGeneration: Generation[S] = Generation[S](global = ())

  def defaultEvaluation: Evaluation[S] = EvalConst()

  def defaultSelection: Selection[S] = SelectTrunc()

  def defaultBreeding: Breeding[S] = ???

  val chromosomeClassTag  = reflect.classTag[Chromosome]
  val selfTypeTag         = ru     .typeTag [TestSys   ]
}

case class EvalConst(d: Double = 0.0) extends Evaluation[TestSys] {
  def apply(sq: TestSys#Chromosome): Double = d
}

case class SelectTrunc(size: SelectionSize = SelectionPercent()) extends Selection[TestSys] {
  override def apply(pop: TestSys#GenomeVal, rnd: util.Random): TestSys#Genome = {
    val n       = size(pop.size)
    val sorted  = pop.sortBy(_._2)
    sorted.takeRight(n).map(_._1)
  }
}
