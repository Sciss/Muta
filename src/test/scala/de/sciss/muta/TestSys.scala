package de.sciss
package muta

import de.sciss.guiflitz.AutoView
import scala.util.Random
import collection.breakOut

object TestSys extends System {
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

  case class Breeding(elitism       : SelectionSize      = SelectionNumber(5),
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

case class SelectTrunc(size: SelectionSize = SelectionPercent())
  extends impl.SelectionTruncationImpl[TestSys.Chromosome] with TestSys.Selection

object CrossoverImpl extends impl.CrossoverVecImpl[Boolean, TestSys.Global] with TestSys.BreedingFunction

object MutationImpl extends impl.MutationVecImpl[Boolean, TestSys.Global] with TestSys.BreedingFunction {
  def mutate(gene: Boolean)(implicit r: util.Random) = !gene
}