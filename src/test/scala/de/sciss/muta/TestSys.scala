package de.sciss
package muta

import de.sciss.guiflitz.{Cell, AutoView}
import scala.util.Random
import collection.breakOut
import scala.swing.Component
import javax.swing.{JTextField, DefaultCellEditor}

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

  def generationView(init: Generation, config: AutoView.Config) = AutoView[Generation](init, config)
  def evaluationViewOption = Some(evaluationView)
  def evaluationView(init: Evaluation, config: AutoView.Config) = AutoView[Evaluation](init, config)
  def selectionView (init: Selection , config: AutoView.Config) = AutoView[Selection ](init, config)
  def breedingView  (init: Breeding  , config: AutoView.Config) = AutoView[Breeding  ](init, config)

  override def chromosomeView(c: Chromosome, default: swing.Label, selected: Boolean,
                              focused: Boolean): swing.Component = {
    if (c != null) default.text = chromoToText(c)
    default
  }

  private def textToChromo(text: String): Chromosome = text.map {
    case '1'  => true
    case _    => false
  }

  private def chromoToText(c: Chromosome): String = c.map(if (_) '1' else '0')(breakOut)

  override def humanEvaluationSteps = 6

  override def hasHumanSelection = true

  private val chromoTextField = new swing.TextField(16)
  // private val chromoEditorComponent     = new DefaultCellEditor(chromoEditorComponentTxt)

  //  private def configureChromoEditor(c: Chromosome): Unit =
  //    chromoEditorComponent.getTableCellEditorComponent(null, c, true, 0, 0)

  override lazy val chromosomeEditorOption: Option[(swing.Component, () => Chromosome, Chromosome => Unit)] =
    Some(chromoTextField, () => textToChromo(chromoTextField.text), c => chromoTextField.text = chromoToText(c))
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