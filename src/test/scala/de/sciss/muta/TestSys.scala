package de.sciss
package muta

import de.sciss.guiflitz.AutoView
import scala.util.Random
import collection.breakOut

object TestSys extends Sys {
  type S          = TestSys.type

  type Chromosome = Vec[Boolean]
  type Global     = Int // number of bits

  val chromosomeClassTag  = reflect.classTag[Chromosome]

  case class Generation(size: Int = 100, global: Int = 32, seed: Int = 0) extends muta.Generation[Chromosome, Global] {
    override def apply(random: Random): Chromosome = Vec.fill(global)(random.nextBoolean())
  }

  // type Generation = muta.Generation[S]
  sealed trait Evaluation extends muta.Evaluation[Chromosome]
  sealed trait Selection  extends muta.Selection [Chromosome]
  sealed trait BreedingFunction extends muta.BreedingFunction[TestSys.Chromosome, TestSys.Global]

  case class Breeding(elitism        : SelectionSize      = SelectionNumber(5),
                     crossoverWeight: SelectionPercent   = SelectionPercent(80),
                     crossover      : BreedingFunction   = CrossoverImpl,
                     mutation       : BreedingFunction   = MutationImpl)
    extends impl.BreedingImpl[Chromosome, Global]

  def defaultGeneration: Generation = Generation()
  def defaultEvaluation: Evaluation = EvalMatchConst()
  def defaultSelection : Selection  = SelectTrunc()
  def defaultBreeding  : Breeding   = Breeding()

  // lazy val selfTypeTag        = ru     .typeTag [TestSys.type]
  // lazy val generationTypeTag  = ru     .typeTag [Generation]

  def generationView(config: AutoView.Config) = AutoView[Generation](defaultGeneration, config)
  def evaluationView(config: AutoView.Config) = AutoView[Evaluation](defaultEvaluation, config)
  def selectionView (config: AutoView.Config) = AutoView[Selection ](defaultSelection , config)
  def breedingView  (config: AutoView.Config) = AutoView[Breeding  ](defaultBreeding  , config)

  override def chromosomeView(c: Chromosome, default: swing.Label, selected: Boolean,
                              focused: Boolean): swing.Component = {
    default.text = c.map(if (_) '1' else '0')(breakOut): String
    default
  }
}

case class EvalMatchConst(target: Boolean = false) extends TestSys.Evaluation {
  def apply(sq: TestSys.Chromosome): Double = sq.count(_ == target).toDouble / sq.size
}

case class SelectTrunc(size: SelectionSize = SelectionPercent()) extends TestSys.Selection {
  override def apply(pop: TestSys.GenomeVal, rnd: util.Random): TestSys.Genome = {
    val n       = size(pop.size)
    val sorted  = pop.sortBy(_._2)
    sorted.takeRight(n).map(_._1)
  }
}

object CrossoverImpl extends TestSys.BreedingFunction {
  def apply(gen: TestSys.Genome, num: Int, glob: TestSys.Global, rand: util.Random): TestSys.Genome = Vec.fill(num) {
    val i   = rand.nextInt(gen.size)
    val j   = rand.nextInt(gen.size)
    val gi  = gen(i)
    val gj  = gen(j)
    val x   = rand.nextInt(math.min(gi.size, gj.size))
    gi.take(x) ++ gj.drop(x)
  }
}

object MutationImpl extends TestSys.BreedingFunction {
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
