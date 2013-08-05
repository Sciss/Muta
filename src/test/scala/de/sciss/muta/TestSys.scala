package de.sciss
package muta

import reflect.runtime.{universe => ru}

trait TestSysLike extends Sys {
  type Chromosome = Vec[Boolean]
  type Global     = Unit

  val chromosomeClassTag  = reflect.classTag[Chromosome]
  // val generationTypeTag   = ru     .typeTag [S#Generation]
}

object TestSys extends TestSysLike {
  type S          = TestSys.type

  case class Generation(size: Int = 100, global: Unit = (), seed: Int = 0) extends muta.Generation[Global]

  // type Generation = muta.Generation[S]
  type Evaluation = muta.Evaluation[Chromosome]
  type Selection  = muta.Selection [Chromosome]
  type Breeding   = muta.Breeding  [Chromosome, Global]

  def defaultGeneration: Generation = Generation()
  def defaultEvaluation: Evaluation = EvalConst()
  def defaultSelection : Selection  = SelectTrunc()
  def defaultBreeding  : Breeding   = Breeding(crossover = CrossoverImpl, mutation = MutationImpl)

  // lazy val selfTypeTag        = ru     .typeTag [TestSys.type]
  // lazy val generationTypeTag  = ru     .typeTag [Generation]
}

case class EvalConst(d: Double = 0.0) extends Evaluation[TestSys.Chromosome] {
  def apply(sq: TestSys.Chromosome): Double = d
}

case class SelectTrunc(size: SelectionSize = SelectionPercent()) extends Selection[TestSys.Chromosome] {
  override def apply(pop: TestSys.GenomeVal, rnd: util.Random): TestSys.Genome = {
    val n       = size(pop.size)
    val sorted  = pop.sortBy(_._2)
    sorted.takeRight(n).map(_._1)
  }
}

object CrossoverImpl extends BreedingFunction[TestSys.Chromosome, TestSys.Global] {
  def apply(gen: TestSys.Genome, num: Int, glob: TestSys.Global, rand: util.Random): TestSys.Genome = Vec.fill(num) {
    val i   = rand.nextInt(gen.size)
    val j   = rand.nextInt(gen.size)
    val gi  = gen(i)
    val gj  = gen(j)
    val x   = rand.nextInt(math.min(gi.size, gj.size))
    gi.take(x) ++ gj.drop(x)
  }
}

object MutationImpl extends BreedingFunction[TestSys.Chromosome, TestSys.Global] {
  def apply(gen: TestSys.Genome, num: Int, glob: TestSys.Global, rand: util.Random): TestSys.Genome = Vec.fill(num) {
    val i   = rand.nextInt(gen.size)
    val gi  = gen(i)
    val mut = rand.nextInt(gi.size / 10 + 1 )
    (gi /: (1 to mut)) { (gj, _) =>
      val j = rand.nextInt(gj.size)
      gj.updated(j, !gj(j))
    }
  }
}
