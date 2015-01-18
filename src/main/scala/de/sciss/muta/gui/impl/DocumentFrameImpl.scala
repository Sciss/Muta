/*
 *  DocumentFrameImpl.scala
 *  (Muta)
 *
 *  Copyright (c) 2013-2014 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v3+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.muta
package gui
package impl

import java.awt.EventQueue
import javax.swing.{SpinnerNumberModel, JCheckBox, SwingConstants}

import de.sciss.desktop.impl.WindowImpl
import de.sciss.desktop.{DialogSource, FileDialog, Window}
import de.sciss.file._
import de.sciss.guiflitz.AutoView
import de.sciss.processor.Processor
import de.sciss.processor.impl.ProcessorImpl
import de.sciss.rating.Rating
import de.sciss.rating.j.DefaultRatingModel
import de.sciss.swingplus.Spinner
import de.sciss.treetable.j.{DefaultTreeTableCellEditor, DefaultTreeTableSorter}
import de.sciss.treetable.{AbstractTreeModel, TreeColumnModel, TreeTable, j}
import play.api.libs.json.{JsPath, JsArray, JsBoolean, JsError, JsNumber, JsObject, JsResult, JsSuccess, JsValue, Json, Reads, Writes}

import scala.annotation.switch
import scala.collection.breakOut
import scala.collection.immutable.{IndexedSeq => Vec}
import scala.concurrent.{ExecutionContext, Future}
import scala.swing.Swing._
import scala.swing.event.{ButtonClicked, KeyTyped}
import scala.swing.{Action, BorderPanel, BoxPanel, Button, CheckBox, FlowPanel, Label, Orientation, ScrollPane, SplitPane}
import scala.util.{Failure, Success, Try}

final class DocumentFrameImpl[S <: System](val application: GeneticApp[S]) extends DocumentFrame[S] { outer =>
  type S1 = S

  val system: S1 = application.system

  import outer.{application => app, system => sys}

  private type SysSettings = Settings { type S = sys.type }

  private def defaultFitness = if (sys.hasHumanEvaluation) 0.0 else Double.NaN

  final class Node(val index: Int, var chromosome: sys.Chromosome, var fitness: Double = defaultFitness,
                   var selected: Boolean = false, val children: Vec[Node] = Vec.empty)
    extends DocumentFrame.NodeLike[sys.Chromosome] {

    override def toString = s"Node(index = $index, chromosome = $chromosome, fitness = $fitness, selected = $selected)"
  }

  import ExecutionContext.Implicits.global

  var random      = sys.rng(0L)
  var evaluation: sys.Evaluation  = sys.defaultEvaluation  // EvalWindowed()
  var selection : sys.Selection   = sys.defaultSelection   // Roulette    ()
  var breeding  : sys.Breeding    = sys.defaultBreeding    // Breeding    ()
  var generation: sys.Generation  = sys.defaultGeneration
  // def generation        : sys.Generation        = pGen .cell()
  // def generation_=(value: sys.Generation): Unit = pGen.cell() = value
  def info        : HeaderInfo        = pInfo.cell()
  def info_=(value: HeaderInfo): Unit = pInfo.cell() = value
  def iterations        : Int        = info.iterations
  def iterations_=(value: Int): Unit = pInfo.cell() = info.copy(iterations = value)

  lazy val avCfg  = {
    val res = AutoView.Config()
    res.scroll    = false
    res.small     = true
    res
  }
  lazy val pGen        = {
    sys.generationView(sys.defaultGeneration, avCfg)
  }
  //    form"""   Duration:|$ggDur |\u2669
  //          |       Seed:|$ggSeed|$ggRandSeed
  //          | Population:|$ggPop |"""
  lazy val pInfo = AutoView(HeaderInfo(), avCfg)

  //                                       index            fitness selected
  type ColMTop = TreeColumnModel.Tuple4[Node, Int, sys.Chromosome, Double, Boolean]
  type ColMBot = TreeColumnModel.Tuple2[Node, Int, sys.Chromosome]

  val seqCol    = new TreeColumnModel.Column[Node, Int]("Index") {
    def apply     (node: Node): Int = node.index
    def update    (node: Node, value: Int) = ()
    def isEditable(node: Node) = false
  }

  val chromoCol = new TreeColumnModel.Column[Node, sys.Chromosome]("Chromosome")(sys.chromosomeClassTag) {
    def apply     (node: Node): sys.Chromosome = node.chromosome
    def update    (node: Node, value: sys.Chromosome): Unit = node.chromosome = value
    def isEditable(node: Node) = sys.hasChromosomeEditor
  }

  val fitCol    = new TreeColumnModel.Column[Node, Double]("Fitness") {
    def apply     (node: Node): Double = node.fitness
    def update    (node: Node, value: Double): Unit = node.fitness = value
    def isEditable(node: Node) = sys.hasHumanEvaluation
  }

  val selCol    = new TreeColumnModel.Column[Node, Boolean]("Selected") {
    def apply     (node: Node): Boolean = node.selected
    def update    (node: Node, value: Boolean): Unit = node.selected = value
    def isEditable(node: Node) = sys.hasHumanSelection
  }

  val tcmTop = new ColMTop(seqCol, chromoCol, fitCol, selCol) {
    def getParent(node: Node) = None
  }

  val tcmBot = new ColMBot(seqCol, chromoCol) {
    def getParent(node: Node) = None
  }

  val rating: Option[Rating] = if (sys.hasHumanEvaluation) {
      val rm  = new DefaultRatingModel(sys.humanEvaluationSteps - 1)
      val r   = new Rating(rm)
      r.focusable = false
      Some(r)
  } else None

  def adjustColumns(tt: TreeTable[_, _]): Unit = {
    val tabCM = tt.peer.getColumnModel
    val sz    = tabCM.getColumnCount
    tabCM.getColumn(0).setPreferredWidth( 48)
    tabCM.getColumn(0).setMaxWidth      ( 48)
    tabCM.getColumn(1).setPreferredWidth(768)
    if (sz >= 4) {
      val fitWidth = rating.fold(72)(_.preferredSize.width + 48)  // account for table column sort-icon width!
      tabCM.getColumn(2).setPreferredWidth(fitWidth)
      tabCM.getColumn(2).setMaxWidth      (128)
      tabCM.getColumn(3).setPreferredWidth( 56) // XXX TODO: should be rendered as checkbox not string
      tabCM.getColumn(3).setMaxWidth      ( 56) // XXX TODO: should be rendered as checkbox not string
    }
  }

  abstract class TreeModel extends AbstractTreeModel[Node] {
    var root = new Node(index = -1, chromosome = null.asInstanceOf[sys.Chromosome])

    def getChildCount(parent: Node            ): Int  = parent.children.size
    def getChild     (parent: Node, index: Int): Node = parent.children(index)

    def isLeaf(node: Node): Boolean = getChildCount(node) == 0

    def valueForPathChanged(path: TreeTable.Path[Node], newValue: Node) = ()

    def getIndexOfChild(parent: Node, child: Node): Int = parent.children.indexOf(child)

    def getParent(node: Node) = if (node == root) None else Some(root)

    protected def adjustColumns(): Unit

    def updateNodes(nodes: Vec[Node]): Unit = {
      // val old = root.children
      // root.children = Vec.empty
      // fireNodesRemoved(old: _*)
      // root.children = nodes
      root = new Node(index = -1, chromosome = null.asInstanceOf[sys.Chromosome], children = nodes)
      // fireNodesInserted(nodes: _*)
      fireStructureChanged(root)
      adjustColumns()
      // fireRootChanged()
    }

    def refreshNodes(nodes: Vec[Node] = root.children): Unit = fireNodesChanged(nodes: _*)
  }

  object tmTop extends TreeModel {
    protected def adjustColumns(): Unit = outer.adjustColumns(mainTable)
  }

  object tmBot extends TreeModel {
    protected def adjustColumns(): Unit = outer.adjustColumns(breedingTable)
  }

  private class TreeRenderer(rating: Option[Rating]) extends j.DefaultTreeTableCellRenderer {
    renderer =>

    // override def setBorder(border: Border) = ()

    private lazy val wrap = new Label { override lazy val peer = renderer }
    private val check = new CheckBox()

    override def getTreeTableCellRendererComponent(treeTable: j.TreeTable, value: Any, selected: Boolean,
                                                   hasFocus: Boolean, row: Int, column: Int,
                                                   expanded: Boolean, leaf: Boolean): java.awt.Component =
      getTreeTableCellRendererComponent(treeTable, value, selected, hasFocus, row, column)

    override def getTreeTableCellRendererComponent(treeTable: j.TreeTable, value: Any, selected: Boolean,
                                                   hasFocus: Boolean, row: Int, column: Int): java.awt.Component = {
      try {
        super.getTreeTableCellRendererComponent(treeTable, value, selected, hasFocus, row, column)
      } catch {
        case _: NullPointerException => // Suckers
      }
      val c1 = treeTable.convertColumnIndexToModel(column)
      setHorizontalAlignment(if (c1 == 0) SwingConstants.RIGHT else SwingConstants.LEFT)
      (c1: @switch) match {
        //        case 0 => // index
        //           // wrap.icon = EmptyIcon
        //          setHorizontalAlignment(SwingConstants.RIGHT)
        //          this

        case 1 => // chromosome
          val swing = sys.chromosomeView(value.asInstanceOf[sys.Chromosome],
            default = wrap, selected = selected, focused = hasFocus)
          swing.peer

        case 2 => // fitness
          rating.fold[java.awt.Component](this) { r =>
            r.value = value match {
              case fit: Double => (fit * r.maximum + 0.5).toInt
              case _ => 0
            }
            // r.border      = EmptyBorder
            r.background  = wrap.background
            r.peer
          }

        case 3 => // selection
          check.background  = wrap.background
          check.selected    = value == true
          check.peer

        case _ =>
          // setHorizontalAlignment(SwingConstants.LEFT)
          this
      }
    }
  }

  def mkTreeTable[Col <: TreeColumnModel[Node]](tm: TreeModel, tcm: Col): TreeTable[Node, Col] = {
    val tt                  = new TreeTable[Node, Col](tm, tcm)
    tt.rootVisible          = false
    tt.autoCreateRowSorter  = true
    tt.peer.setNodeSortingEnabled(false)
    val dtts = tt.peer.getRowSorter.asInstanceOf[DefaultTreeTableSorter[_, _, _]]
    dtts.setSortsOnUpdates(true)
    dtts.setComparator(0, Ordering.Int)
    if (tcm.columnCount >= 4) {
      dtts.setComparator(2, Ordering.Double)
      dtts.setComparator(3, Ordering.Boolean)
    }

    val tr = new TreeRenderer(rating)
    // tt.peer.setDefaultRenderer(null, tr)
    tt.peer.setDefaultRenderer(sys.chromosomeClassTag.runtimeClass, tr)
    tt.peer.setDefaultRenderer(classOf[Int]       , tr) // TreeTableCellRenderer.Default.peer
    tt.peer.setDefaultRenderer(classOf[Double]    , tr) // TreeTableCellRenderer.Default.peer
    tt.peer.setDefaultRenderer(classOf[Boolean]   , tr) // TreeTableCellRenderer.Default.peer

    if (sys.hasHumanSelection)
      tt.peer.setDefaultEditor(classOf[Boolean], new DefaultTreeTableCellEditor(new JCheckBox()))

    sys.chromosomeEditorOption.foreach { case (editor, getter, setter) =>
      tt.peer.setDefaultEditor(sys.chromosomeClassTag.runtimeClass,
        ChromosomeEditor[sys.Chromosome](editor)(getter())(setter))
    }

    rating.foreach { r =>
      tt.peer.setDefaultEditor(classOf[Double], new RatingEditor(r))

      tt.listenTo(tt.keys)
      tt.reactions += {
        case KeyTyped(_, ch, 0, _) if ch >= '0' && ch <= '9' =>
          val fit = math.min(r.maximum, ch - '0').toDouble / r.maximum
          // println(s"FIT $fit")
          val ns = selectedNodes
          ns.foreach { n =>
            n.fitness = fit
          }
          tm.refreshNodes(ns)
          mainTable.repaint() // XXX TODO should not be necessary
      }
    }

    adjustColumns(tt)
    tt
  }

  val mainTable     = mkTreeTable(tmTop, tcmTop)
  val ggScrollTop   = new ScrollPane(mainTable)
  val breedingTable = mkTreeTable(tmBot, tcmBot)
  val ggScrollBot   = new ScrollPane(breedingTable)

  val topPanel      = new FlowPanel(pInfo.component)

  val pTopSettings = new BoxPanel(Orientation.Vertical) {
    contents += topPanel  // XXX TODO: needs FlowPanel for correct layout
    contents += VStrut(4)
    // contents += ggGen
  }

  def currentTable        : Vec[Node]         = tmTop.root.children
  def currentTable_=(nodes: Vec[Node]): Unit  = tmTop.updateNodes(nodes)

  def selectedNodes = mainTable.selection.paths.map(_.last).toIndexedSeq.sortBy(-_.fitness)

  type Document = (Vec[Node], SysSettings)

  def settings: SysSettings = Settings(sys)(info, generation, evaluation, selection, breeding)
  def settings_=(s: SysSettings): Unit = {
    evaluation  = s.evaluation
    selection   = s.selection
    breeding    = s.breeding
    info        = s.info
    generation  = s.generation
  }

  def iterate(n: Int, quiet: Boolean): Future[Unit] = {
    mNumIter.setValue(n)
    ggIter.perform(quiet = quiet)
  }

  def stepEval(fun: sys.Evaluation, glob: sys.Global, genome: Vec[Node], progress: Float => Unit = _ => ()): Unit = {
    var min   = Double.MaxValue
    var max   = Double.MinValue
    val sz    = genome.size
    genome.zipWithIndex.foreach { case (node, idx) =>
      val f = fun(node.chromosome, glob)
      node.fitness = f
      if (f < min) min = f
      if (f > max) max = f
      progress((idx + 1).toFloat / sz)
    }
    // normalize
    if (max > min) {
      val off     = -min
      val scale   = 1.0/(max - min)
      // XXX TODO -- should not do this on the processing thread
      genome.foreach { node =>
        node.fitness = (node.fitness + off) * scale
      }
    }
  }

  def stepSelect(genome: Vec[Node]): Unit = {
    val fun       = selection
    val selected  = fun(genome.map(node => (node.chromosome, node.fitness)), random).toSet
    // XXX TODO -- should not do this on the processing thread
    genome.foreach { node =>
      node.selected = selected.contains(node.chromosome)
    }
  }

  def stepBreed(genome: Vec[Node]): Vec[Node] = {
    val fun   = breeding
    val glob  = generation.global
    val r     = random
    val n     = fun(genome.map(node => (node.chromosome, node.fitness, node.selected)), glob, r)

    lazy val fitMap = (genome.map(n => n.chromosome -> n.fitness)(breakOut):
      Map[sys.Chromosome, Double]) withDefaultValue defaultFitness

    val fitFun: sys.Chromosome => Double = if (sys.hasHumanEvaluation) fitMap.apply else _ => defaultFitness

    n.zipWithIndex.map { case (c, idx) => new Node(index = idx, chromosome = c, fitness = fitFun(c)) }
  }

  def defer(thunk: => Unit): Unit =
    if (EventQueue.isDispatchThread) thunk else onEDT(thunk)

  val settingsViewConfig = {
    val res = AutoView.Config()
    res.small = true
    res.build
  }

  private var windowMap = Map.empty[String, Window]

  def mkSettingsButton[A](title: String)(view: (A, AutoView.Config) => AutoView[A])
                         (getter: => A)(setter: A => Unit): Button = {
    val but = Button("Settings") {
      windowMap.get(title) match {
        case Some(w) => w.front()
        case _ =>
          val av  = view(getter, settingsViewConfig)
          val sf  = new SettingsFrame(app, av, title = title)
          val w   = sf.window
          av.cell.addListener {
            case value => setter(value)
          }
          windowMap += title -> w
          w.reactions += {
            case Window.Closing(_) =>
              // println(s"Closing $title Settings")
              windowMap -= title
          }
      }
    }
    but.peer.putClientProperty("JButton.buttonType", "segmentedCapsule")
    but.peer.putClientProperty("JButton.segmentPosition", "last")
    but
  }

  private object ProcButton {
    def apply(label: String)(mkProc: => Processor[Any] with Processor.Prepared): ProcButton =
      new ProcButton(label, mkProc)
  }
  private class ProcButton(label: String, mkProc: => Processor[Any] with Processor.Prepared) extends Button(label) {
    var proc      = Option.empty[Processor[Any]]
    val progIcon  = new ProgressIcon(33)

    listenTo(this)
    reactions += {
      case ButtonClicked(_) =>
        proc.fold[Unit](perform(quiet = false))(_.abort())
    }

    def perform(quiet: Boolean): Future[Unit] = {
      proc.foreach(_.abort())
      val p   = mkProc
      proc    = Some(p)
      p.addListener {
        case prog @ Processor.Progress(_, _) => defer {
          progIcon.value = prog.toInt
          repaint()
        }
      }
      p.start()
      text            = "\u2716"
      progIcon.value  = 0
      icon            = progIcon
      p.onComplete {
        case res => defer {
          icon  = EmptyIcon
          text  = label
          if (proc == Some(p)) proc = None
          if (!quiet) res match {
            case Failure(ex) => DialogSource.Exception(ex -> label).show(Some(outer.window))
            case _ =>
          }
        }
      }
      p.map(_ => ())
    }

    peer.putClientProperty("JButton.buttonType", "segmentedCapsule")
    peer.putClientProperty("JButton.segmentPosition", "first")
    preferredSize = (84, preferredSize.height)
    minimumSize   = preferredSize
    maximumSize   = preferredSize
  }

  private val mNumIter  = new SpinnerNumberModel(10, 1, 10000, 1)
  private val ggNumIter = new Spinner(mNumIter)
  ggNumIter.tooltip = "Number of iterations to perform at once"
  private val ggIter = ProcButton("Iterate") {
    val num = mNumIter.getNumber.intValue()
    val in  = currentTable
    val p   = new IterProc(in, num, generation.global, evaluation)
    p.onSuccess {
      case out => defer {
        tmTop.updateNodes(out)
        iterations += num
        tmBot.updateNodes(Vec.empty)
      }
    }
    p
  }

  val pButtons = new FlowPanel {
    contents += new BoxPanel(Orientation.Horizontal) {
      val ggGen = ProcButton("Generate") {
        val p = new GenProc(generation)
        p.onSuccess {
          case nodes => defer {
            tmTop.updateNodes(nodes)
            iterations = 0
          }
        }
        p
      }

      val ggGenSettings = mkSettingsButton[sys.Generation]("Generation")(sys.generationView)(generation)(generation = _)

      val evalOpt = sys.evaluationViewOption.map { evalViewFun =>
        val ggEval = ProcButton("Evaluate") {
          val s = new EvalProc(evaluation, generation.global, currentTable)
          s.onSuccess {
            case _ => defer {
              tmTop.refreshNodes()
              mainTable.repaint() // XXX TODO should not be necessary
            }
          }
          s
        }
        ggEval.peer.putClientProperty("JButton.buttonType", "segmentedCapsule")
        ggEval.peer.putClientProperty("JButton.segmentPosition", "first")
        val ggEvalSettings = mkSettingsButton[sys.Evaluation]("Evaluation")( (init, config) =>
          evalViewFun(init, config) )(evaluation)(evaluation = _)

        (ggEval, ggEvalSettings)
      }

      val ggSel = Button("Select") {
        stepSelect(currentTable)
        tmTop.refreshNodes()
        mainTable.repaint() // XXX TODO should not be necessary
      }
      ggSel.peer.putClientProperty("JButton.buttonType", "segmentedCapsule")
      ggSel.peer.putClientProperty("JButton.segmentPosition", "first")
      val ggSelSettings = mkSettingsButton[sys.Selection]("Selection")(sys.selectionView)(selection)(selection = _)

      val ggBreed = Button("Breed") {
        val newNodes = stepBreed(currentTable)
        tmBot.updateNodes(newNodes)
      }
      ggBreed.peer.putClientProperty("JButton.buttonType", "segmentedCapsule")
      ggBreed.peer.putClientProperty("JButton.segmentPosition", "first")
      val ggBreedSettings = mkSettingsButton[sys.Breeding]("Breeding")(sys.breedingView)(breeding)(breeding = _)

      val ggFeed = Button("\u21E7") {
        tmTop.updateNodes(tmBot.root.children)
        iterations += 1
      }
      ggFeed.peer.putClientProperty("JButton.buttonType", "segmentedCapsule")
      ggFeed.peer.putClientProperty("JButton.segmentPosition", "only")
      ggFeed.tooltip = "Feed offspring back for next iteration"

      contents ++= Seq(            ggGen  , ggGenSettings)
      evalOpt.foreach { case (ggEval, ggEvalSettings) => contents ++= Seq(HStrut( 8), ggEval , ggEvalSettings) }
      contents ++= Seq(HStrut( 8), ggSel  , ggSelSettings  ,
                       HStrut( 8), ggBreed, ggBreedSettings,
                       HStrut( 8), ggFeed,
                       HStrut( 8), ggNumIter, ggIter)
    }
  }

  val splitTop = new BorderPanel {
    add(pTopSettings, BorderPanel.Position.North )
    add(ggScrollTop , BorderPanel.Position.Center)
    add(pButtons    , BorderPanel.Position.South )
  }

  val splitBot = new BoxPanel(Orientation.Horizontal) {
    contents += ggScrollBot
    contents += HStrut(128 + 7)
  }

  val ggSplit = new SplitPane(Orientation.Horizontal) {
    resizeWeight    = 0.5
    topComponent    = splitTop
    bottomComponent = splitBot
  }

  private def settingsFieldsToJson(): Vec[(String, JsValue)] = {
    val v1 = Vec(
      "info"        -> Json.format[HeaderInfo].writes(settings.info      ),
      "generation"  -> sys.generationFormat   .writes(settings.generation),
      // "evaluation"  -> sys.evaluationFormat   .writes(settings.evaluation),
      "selection"   -> sys.selectionFormat    .writes(settings.selection ),
      "breeding"    -> sys.breedingFormat     .writes(settings.breeding  )
    )
    if (sys.evaluationViewOption.isEmpty) v1 else {
      v1 :+ "evaluation" -> sys.evaluationFormat.writes(settings.evaluation)
    }
  }

  private def settingsFieldsFromJson(fields: Map[String, JsValue]): JsResult[SysSettings] = {
    val res = Try(for {
      info    <- Json.format[HeaderInfo].reads(fields("info"      ))
      gen     <- sys.generationFormat   .reads(fields("generation"))
      // eval    <- sys.evaluationFormat   .reads(fields("evaluation"))
      select  <- sys.selectionFormat    .reads(fields("selection" ))
      breed   <- sys.breedingFormat     .reads(fields("breeding"  ))
    } yield {
      val eval = fields.get("evaluation").fold(evaluation) { f =>
        sys.evaluationFormat.reads(f).get
      }
      Settings(sys)(info, gen, eval, select, breed)
    })
    res match {
      case Success(s) => s
      case Failure(e) => JsError(e.getMessage)
    }
  }

  object window extends WindowImpl { me =>
    def handler = app.windowHandler
    contents    = ggSplit

    def saveDialog(): Option[File] = {
      val dlg = FileDialog.save(title = "Save Document", init = file)
      dlg.show(Some(me))
    }

    def save(f: File, quiet: Boolean): Try[Unit] = {
      // Note: cannot use auto formats here, because node indices are implied from JsArray indices
      implicit val documentW = Writes[Document] { case (nodes, settings) =>
        JsObject(settingsFieldsToJson() :+ ("genome" -> JsArray(nodes.map { n =>
          val sq0 = "selected" -> (JsBoolean(n.selected): JsValue) :: Nil
          val sq1 = if (n.fitness.isNaN) sq0 else ("fitness" -> JsNumber(n.fitness)) :: sq0
          val sq2 = ("chromosome" -> sys.chromosomeFormat.writes(n.chromosome)) :: sq1
          JsObject(sq2)
        })))
      }
      val res = SettingsIO.write(currentTable -> settings, f.replaceExt("json"))
      res match {
        case Success(_) =>
          file = Some(f)
          updateTitle()
        case Failure(e: Exception) =>
          if (!quiet) showDialog(e -> "Save Document")
        case Failure(e) => e.printStackTrace()
      }
      res
    }

    def updateTitle(): Unit = title = file.fold(app.name)(f => s"${f.base} : ${app.name}")

    updateTitle()

    // XXX TODO
    //    bindMenu("file.export.lily", Action("") {
    //      val nodes = selectedNodes
    //      if (nodes.nonEmpty) {
    //        ExportLilypond.dialog(settings, nodes.map(n => (n.chromosome, n.fitness)))
    //      }
    //    })
    bindMenu("file.export.settings", Action("") {
      val dlg = FileDialog.save(title = "Export Algorithm Settings")
      dlg.show(Some(me)).foreach { f =>
        implicit val settingsW = Writes[SysSettings] { settings =>
          JsObject(settingsFieldsToJson())
        }
        SettingsIO.write(settings, f.replaceExt("json"))
      }
    })
    bindMenu("file.save", Action("") {
      (file orElse saveDialog()).foreach(save(_, quiet = false))
    })
    bindMenu("file.save-as", Action("") {
      saveDialog().foreach(save(_, quiet = false))
    })

    //    bindMenu("file.export.table", Action("") {
    //      val nodes = selectedNodes
    //      if (nodes.nonEmpty) {
    //        val dlg = FileDialog.save(title = "Export Selection as PDF Table")
    //        dlg.show(Some(me)).foreach(f => exportTableAsPDF(f, nodes.map(n => (n.chromosome, n.fitness))))
    //      }
    //    })

    // XXX TODO
    //    bindMenu("file.import.settings", Action("") {
    //      val dlg = FileDialog.open(title = "Import Algorithm Settings")
    //      dlg.show(Some(me)).foreach { f =>
    //        settings = SettingsIO.read(f)
    //      }
    //    })
    // pack()
    // front()

    def open(): Unit = {
      pack()
      val w = window.component.peer
      val gc = w.getGraphicsConfiguration
      if (gc != null) {
        val r1    = gc.getBounds
        val r2    = w . getBounds
        val hOver = r2.x + r2.width  > r1.x + r1.width
        val vOver = r2.y + r2.height > r1.y + r1.height
        if (hOver || vOver) {
          if (hOver) {
            r2.x      = r1.x
            r2.width  = r1.width
          }
          if (vOver) {
            r2.y      = r1.y
            r2.height = r1.height
          }
          w.setBounds(r2)
        }
      }
      front()
    }

    def bindMenu2(path: String, action: Action): Unit = bindMenu(path, action)

    def load(file: File, quiet: Boolean): Try[Unit] = {
      // Note: cannot use auto formats here, because node indices are implied from JsArray indices
      val settingsR = Reads[Document] {
        case JsObject(sq) =>
          val m = sq.toMap
          for {
            _settings <- settingsFieldsFromJson(m)
            _genome   <- m.get("genome").fold[JsResult[Vec[Node]]](JsError(s"Field for genome not found")) {
              case JsArray(nj) =>
                val nodes = ((JsSuccess(Vec.empty): JsResult[Vec[Node]]) /: nj.zipWithIndex) {
                  case (res, (nj0, idx)) =>
                    val res1 = res.flatMap { sq =>
                      nj0 match {
                        case JsObject(nfj) =>
                          val m1                = nfj.toMap
                          val chromosomeJ       = m1("chromosome")
                          val chromosomeT       = sys.chromosomeFormat.reads(chromosomeJ)
                          val fitnessT = m1.get("fitness").fold(JsSuccess(Double.NaN): JsResult[Double]) {
                            case JsNumber(fitNum) => JsSuccess(fitNum.toDouble)
                            case other => JsError(s"Fitness not a JSON number: $other")
                          }
                          val selectedT = m1.get("selected").fold(JsSuccess(false): JsResult[Boolean]) {
                            case JsBoolean(sel) => JsSuccess(sel)
                            case other => JsError(s"Selected not a JSON boolean: $other")
                          }
                          for {
                            chromosome <- chromosomeT
                            fitness    <- fitnessT
                            selected   <- selectedT
                          } yield {
                            val node = new Node(index = idx, chromosome = chromosome, fitness = fitness,
                              selected = selected)
                            sq :+ node
                          }

                        case other => JsError(s"Chromosome entry not a JSON object $other")
                      }
                    }
                    if (res1.isSuccess) res1 else res1.repath(JsPath(idx))  // XXX TODO -- correct?
                }
                nodes

              case other => JsError(s"Genome not a JSON array $other")
            }
          } yield (_genome, _settings)

        case json => JsError(s"Settings not a JSON object $json")
      }

      val res = SettingsIO.read(file)(settingsR)
      res match {
        case Success((gen, set)) =>
          settings      = set
          currentTable  = gen
          me.file       = Some(file)
          updateTitle()

        case Failure(e: Exception) =>
          if (!quiet) showDialog(e -> "Open Document")

        case Failure(e) => e.printStackTrace()
      }
      res.map(_ => ())
    }
  }

  def open(): Unit = window.open()

  def load(file: File, quiet: Boolean): Try[Unit] = window.load(file, quiet = quiet)
  def save(file: File, quiet: Boolean): Try[Unit] = window.save(file, quiet = quiet)

  //  def exportTableAsPDF(f: File, genome: sys.GenomeVal): Unit = {
  //    // XXX TODO
  //    //    import sys.process._
  //    //    val f1 = f.replaceExt("pdf")
  //    //    ExportTable(f1, genome, settings)
  //    //    Seq(pdfViewer, f1.path).!
  //  }

  def bindMenu(path: String, action: Action): Unit = window.bindMenu2(path, action)

  class GenProc(gen: sys.Generation)
    extends ProcessorImpl[Vec[Node], Processor[Vec[Node]]] with Processor[Vec[Node]] {

    protected def body(): Vec[Node] = {
      val pop = gen.size
      val r   = sys.rng(gen.seed)
      Vec.tabulate(pop) { idx =>
        val sq  = gen(r)
        val n   = new Node(index = idx, chromosome = sq)
        checkAborted()
        progress = (idx + 1).toFloat / pop
        n
      }
    }
  }

  class EvalProc(eval: sys.Evaluation, glob: sys.Global, genome: Vec[Node])
    extends ProcessorImpl[Unit, Processor[Unit]] with Processor[Unit] {

    protected def body(): Unit =
      stepEval(eval, glob, genome, p => {
        checkAborted()
        progress = p
      })
  }

  class IterProc(in: Vec[Node], num: Int, glob: sys.Global, eval: sys.Evaluation)
    extends ProcessorImpl[Vec[Node], Processor[Vec[Node]]] with Processor[Vec[Node]] {

    protected def body(): Vec[Node] = {
      // we want to stop the iteration with evaluation, so that the fitness values are shown in the top pane
      // ; ensure that initially the nodes have been evaluation
      if (in.exists(_.fitness.isNaN)) stepEval(eval, glob, in)
      checkAborted()
      val out = (in /: (0 until num)) { (itIn, idx) =>
        stepSelect(itIn)
        val itOut = stepBreed(itIn)
        stepEval(eval, glob, itOut)
        val f = (idx + 1).toFloat / num
        progress = f
        checkAborted()
        itOut
      }
      out
    }
  }
}