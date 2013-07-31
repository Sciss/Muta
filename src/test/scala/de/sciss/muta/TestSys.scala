package de.sciss.muta

import reflect.runtime.{universe => ru}

class TestSys extends Sys[TestSys] {
  type S          = TestSys
  type Chromosome = Vec[Boolean]
  type Global     = Unit

  def defaultGeneration: Generation[S] = Generation[S](global = ())

  def defaultEvaluation: Evaluation[S] = EvalConst()

  def defaultSelection: Selection[S] = SelectTrunc()

  def defaultBreeding: Breeding[S] = Breeding(crossover = CrossoverImpl, mutation = MutationImpl)

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

object CrossoverImpl extends BreedingFunction[TestSys] {
  def apply(gen: TestSys#Genome, num: Int, glob: TestSys#Global, rand: util.Random): TestSys#Genome = Vec.fill(num) {
    val i   = rand.nextInt(gen.size)
    val j   = rand.nextInt(gen.size)
    val gi  = gen(i)
    val gj  = gen(j)
    val x   = rand.nextInt(math.min(gi.size, gj.size))
    gi.take(x) ++ gj.drop(x)
  }
}

object MutationImpl extends BreedingFunction[TestSys] {
  def apply(gen: TestSys#Genome, num: Int, glob: TestSys#Global, rand: util.Random): TestSys#Genome = Vec.fill(num) {
    val i   = rand.nextInt(gen.size)
    val gi  = gen(i)
    val mut = rand.nextInt(gi.size / 10 + 1 )
    (gi /: (1 to mut)) { (gj, _) =>
      val j = rand.nextInt(gj.size)
      gj.updated(j, !gj(j))
    }
  }
}
