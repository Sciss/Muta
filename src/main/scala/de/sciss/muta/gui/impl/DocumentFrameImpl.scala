package de.sciss.muta.gui
package impl

import scala.swing.{Label, Action, SplitPane, FlowPanel, Orientation, Swing, BoxPanel, BorderPanel, ScrollPane, Button}
import Swing._
import de.sciss.desktop.impl.WindowImpl
import de.sciss.desktop.{FileDialog, Window}
import javax.swing.SpinnerNumberModel
import de.sciss.treetable.{AbstractTreeModel, TreeColumnModel, TreeTable, TreeTableCellRenderer, j}
import java.awt.EventQueue
import collection.immutable.{IndexedSeq => Vec}
import de.sciss.swingplus.Spinner
import de.sciss.treetable.j.DefaultTreeTableSorter
import scala.swing.event.ButtonClicked
import de.sciss.file._
import de.sciss.processor.impl.ProcessorImpl
import de.sciss.processor.Processor
import scala.concurrent.ExecutionContext
import de.sciss.guiflitz.AutoView
import de.sciss.muta.{Settings, HeaderInfo, System}
import de.sciss.muta.gui.{ProgressIcon, SettingsFrame, GeneticApp}

final class DocumentFrameImpl[S <: System](val application: GeneticApp[S]) extends DocumentFrame[S] { outer =>
  type S1 = S

  val system: S1 = application.system

  import outer.{system => sys, application => app}

  final class Node(val index: Int, val chromosome: sys.Chromosome, var fitness: Double = Double.NaN,
                   var selected: Boolean = false, val children: Vec[Node] = Vec.empty)

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

  //  val mDur        = new SpinnerNumberModel(16, 1, 128, 1)
  //  val ggDur       = new Spinner(mDur)
  //  val mSeed       = new SpinnerNumberModel(0L, 0L, Long.MaxValue, 1L)
  //  val ggSeed      = new Spinner(mSeed) {
  //    listenTo(this)
  //    reactions += {
  //      case ValueChanged(_) =>
  //        random = Fitness.rng(mSeed.getNumber.longValue())
  //    }
  //  }
  //  val mPop        = new SpinnerNumberModel(100, 1, 10000, 1)
  //  val ggPop       = new Spinner(mPop)
  //  val ggRandSeed  = Button("Rand") {
  //    mSeed.setValue(util.Random.nextLong()) // System.currentTimeMillis())
  //  }

  lazy val avCfg  = {
    val res = AutoView.Config()
    res.scroll    = false
    res.small     = true
    res
  }
  lazy val pGen        = {
    // import system.globalTypeTag
    // import system.generationTypeTag
    // AutoView(system.defaultGeneration, avCfg)
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
    def update    (node: Node, value: sys.Chromosome) = ()
    def isEditable(node: Node) = false
  }

  val fitCol    = new TreeColumnModel.Column[Node, Double]("Fitness") {
    def apply     (node: Node): Double = node.fitness
    def update    (node: Node, value: Double) = ()
    def isEditable(node: Node) = false  // could be...
  }

  val selCol    = new TreeColumnModel.Column[Node, Boolean]("Selected") {
    def apply     (node: Node): Boolean = node.selected
    def update    (node: Node, value: Boolean) = ()
    def isEditable(node: Node) = false  // could be...
  }

  val tcmTop = new ColMTop(seqCol, chromoCol, fitCol, selCol) {
    def getParent(node: Node) = None
  }

  val tcmBot = new ColMBot(seqCol, chromoCol) {
    def getParent(node: Node) = None
  }

  def adjustColumns(tt: TreeTable[_, _]): Unit = {
    val tabcm = tt.peer.getColumnModel
    val sz    = tabcm.getColumnCount
    tabcm.getColumn(0).setPreferredWidth( 48)
    tabcm.getColumn(0).setMaxWidth      ( 48)
    tabcm.getColumn(1).setPreferredWidth(768)
    if (sz >= 4) {
      tabcm.getColumn(2).setPreferredWidth( 72)
      tabcm.getColumn(2).setMaxWidth      (128)
      tabcm.getColumn(3).setPreferredWidth( 56) // XXX TODO: should be rendered as checkbox not string
      tabcm.getColumn(3).setMaxWidth      ( 56) // XXX TODO: should be rendered as checkbox not string
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

    def refreshNodes(): Unit = fireNodesChanged(root.children: _*)
  }

  object tmTop extends TreeModel {
    protected def adjustColumns(): Unit = outer.adjustColumns(mainTable)
  }

  object tmBot extends TreeModel {
    protected def adjustColumns(): Unit = outer.adjustColumns(breedingTable)
  }

  private class ChromosomeRenderer extends j.DefaultTreeTableCellRenderer {
    renderer =>

    private lazy val wrap = new Label { override lazy val peer = renderer }

    override def getTreeTableCellRendererComponent(treeTable: j.TreeTable, value: Any, selected: Boolean,
                                                   hasFocus: Boolean, row: Int, column: Int): java.awt.Component = {
      super.getTreeTableCellRendererComponent(treeTable, value, selected, hasFocus, row, column)
      // XXX TODO: `column` is physical column, not logical column.
      if (column == 1) {
        val swing = sys.chromosomeView(value.asInstanceOf[sys.Chromosome],
          default = wrap, selected = selected, focused = hasFocus)
        swing.peer
      } else {
        this
      }
    }
  }

  def mkTreeTable[Col <: TreeColumnModel[Node]](tm: TreeModel, tcm: Col): TreeTable[Node, Col] = {
    val tt                  = new TreeTable[Node, Col](tm, tcm)
    tt.rootVisible          = false
    tt.autoCreateRowSorter  = true
    val dtts = tt.peer.getRowSorter.asInstanceOf[DefaultTreeTableSorter[_, _, _]]
    dtts.setSortsOnUpdates(true)
    dtts.setComparator(0, Ordering.Int)
    if (tcm.columnCount >= 4) {
      dtts.setComparator(2, Ordering.Double)
      dtts.setComparator(3, Ordering.Boolean)
    }

      // val dtts = new DefaultTreeTableSorter(tm.pee, tcm.peer)
    //  // tt.peer.setRowSorter(dtts)
    //  println(s"Sortable(0)? ${dtts.isSortable(0)}; Sortable(2)? ${dtts.isSortable(2)}")
    //  dtts.setSortable(0, true)
    //  dtts.setSortable(2, true)

    // tt.expandPath(TreeTable.Path.empty)
    // XXX TODO: working around TreeTable issue #1
    tt.peer.setDefaultRenderer(sys.chromosomeClassTag.runtimeClass, new ChromosomeRenderer)  // XXX TODO: to avoid conflict, should specify logical column index
    tt.peer.setDefaultRenderer(classOf[Int]       , TreeTableCellRenderer.Default.peer)
    tt.peer.setDefaultRenderer(classOf[Double]    , TreeTableCellRenderer.Default.peer)
    tt.peer.setDefaultRenderer(classOf[Boolean]   , TreeTableCellRenderer.Default.peer)

    adjustColumns(tt)
    tt
  }

  val mainTable     = mkTreeTable(tmTop, tcmTop)
  val ggScrollTop   = new ScrollPane(mainTable)
  val breedingTable = mkTreeTable(tmBot, tcmBot)
  val ggScrollBot   = new ScrollPane(breedingTable)

  val pTopSettings = new BoxPanel(Orientation.Vertical) {
    contents += new FlowPanel(pInfo.component)  // XXX TODO: needs FlowPanel for correct layout
    contents += VStrut(4)
    // contents += ggGen
  }

  def settings: Settings { type S = sys.type } = Settings(sys)(info, generation, evaluation, selection, breeding)
  def settings_=(s: Settings { type S = sys.type }): Unit = {
    evaluation  = s.evaluation
    selection   = s.selection
    breeding    = s.breeding
    info        = s.info
    generation  = s.generation
  }

  def stepEval(genome: Vec[Node]): Unit = {
    val fun = evaluation
    var min = Double.MaxValue
    var max = Double.MinValue
    genome.foreach { node =>
      val f = fun(node.chromosome)
      node.fitness = f
      if (f < min) min = f
      if (f > max) max = f
    }
    // normalize
    if (max > min) {
      val off     = -min
      val scale   = 1.0/(max - min)
      genome.foreach { node =>
        node.fitness = (node.fitness + off) * scale
      }
    }
  }

  def stepSelect(genome: Vec[Node]): Unit = {
    val fun       = selection
    val selected  = fun(genome.map(node => (node.chromosome, node.fitness)), random).toSet
    genome.foreach { node =>
      node.selected = selected.contains(node.chromosome)
    }
  }

  def stepBreed(genome: Vec[Node]): Vec[Node] = {
    val fun   = breeding
    val glob  = generation.global
    val r     = random
    val n     = fun(genome.map(node => (node.chromosome, node.fitness, node.selected)), glob, r)
    n.zipWithIndex.map { case (c, idx) => new Node(index = idx, chromosome = c)}
  }

  def selectedNodes = mainTable.selection.paths.map(_.last).toIndexedSeq.sortBy(-_.fitness)

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

  val pButtons = new FlowPanel {
    contents += new BoxPanel(Orientation.Horizontal) {
      val ggGen = Button("Generate") {
        implicit val r  = sys.rng(generation.seed)
        random          = r
        val pop         = generation.size
        // val glob        = generation.global
        val nodes       = Vector.tabulate(pop) { idx =>
          val sq  = generation(r)
          new Node(index = idx, chromosome = sq)
        }
        tmTop.updateNodes(nodes)
        iterations = 0
      }
      ggGen.peer.putClientProperty("JButton.buttonType", "segmentedCapsule")
      ggGen.peer.putClientProperty("JButton.segmentPosition", "first")
      val ggGenSettings = mkSettingsButton[sys.Generation]("Generation")(sys.generationView)(generation)(generation = _)

      val ggEval = Button("Evaluate") {
        stepEval(tmTop.root.children)
        tmTop.refreshNodes()
        mainTable.repaint() // XXX TODO should not be necessary
      }
      ggEval.peer.putClientProperty("JButton.buttonType", "segmentedCapsule")
      ggEval.peer.putClientProperty("JButton.segmentPosition", "first")
      val ggEvalSettings = mkSettingsButton[sys.Evaluation]("Evaluation")(sys.evaluationView)(evaluation)(evaluation = _)

      val ggSel = Button("Select") {
        stepSelect(tmTop.root.children)
        tmTop.refreshNodes()
        mainTable.repaint() // XXX TODO should not be necessary
      }
      ggSel.peer.putClientProperty("JButton.buttonType", "segmentedCapsule")
      ggSel.peer.putClientProperty("JButton.segmentPosition", "first")
      val ggSelSettings = mkSettingsButton[sys.Selection]("Selection")(sys.selectionView)(selection)(selection = _)

      val ggBreed = Button("Breed") {
        val newNodes = stepBreed(tmTop.root.children)
        tmBot.updateNodes(newNodes)
      }
      ggBreed.peer.putClientProperty("JButton.buttonType", "segmentedCapsule")
      ggBreed.peer.putClientProperty("JButton.segmentPosition", "first")
      val ggBreedSettings = mkSettingsButton[sys.Breeding]("Breeding")(sys.breedingView)(breeding)(breeding = _)

      val ggFeed = Button("\u21E7") {
        tmTop.updateNodes(tmBot.root.children)
      }
      ggFeed.peer.putClientProperty("JButton.buttonType", "segmentedCapsule")
      ggFeed.peer.putClientProperty("JButton.segmentPosition", "only")
      ggFeed.tooltip = "Feed offspring back for next iteration"

      val mNumIter  = new SpinnerNumberModel(10, 1, 10000, 1)
      val ggNumIter = new Spinner(mNumIter)
      ggNumIter.tooltip = "Number of iterations to perform at once"
      val ggIter = new Button("Iterate") { // \u238C \u260D \u267B
        var proc      = Option.empty[Proc]
        val progIcon  = new ProgressIcon(33)

        listenTo(this)
        reactions += {
          case ButtonClicked(_) =>
            proc match {
              case Some(p) => p.abort()
              case _ =>
                val num = mNumIter.getNumber.intValue()
                val in  = tmTop.root.children
                val p   = new Proc(in, num)
                proc    = Some(p)
                p.addListener {
                  case prog @ Processor.Progress(_, _) => defer {
                    progIcon.value = prog.toInt
                    repaint()
                  }
                }
                import ExecutionContext.Implicits.global
                p.start()
                text            = "\u2716"
                progIcon.value  = 0
                icon            = progIcon
                p.onComplete {
                  case _ => defer {
                    icon  = EmptyIcon
                    text  = "Iterate"
                    proc  = None
                  }
                }
                p.onSuccess {
                  case out => defer {
                    tmTop.updateNodes(out)
                    iterations += num
                    tmBot.updateNodes(Vec.empty)
                  }
                }
            }
        }

        peer.putClientProperty("JButton.buttonType", "segmentedCapsule")
        peer.putClientProperty("JButton.segmentPosition", "last")
        preferredSize = (72, preferredSize.height)
        minimumSize   = preferredSize
        maximumSize   = preferredSize
      }

      contents ++= Seq(            ggGen  , ggGenSettings  ,
                       HStrut( 8), ggEval , ggEvalSettings ,
                       HStrut( 8), ggSel  , ggSelSettings  ,
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

  object window extends WindowImpl { me =>
    def handler = app.windowHandler
    def style   = Window.Regular
    contents    = ggSplit
    title       = "Genetic Algorithm"

    // XXX TODO
    //    bindMenu("file.export.lily", Action("") {
    //      val nodes = selectedNodes
    //      if (nodes.nonEmpty) {
    //        ExportLilypond.dialog(settings, nodes.map(n => (n.chromosome, n.fitness)))
    //      }
    //    })
    //    bindMenu("file.export.settings", Action("") {
    //      val dlg = FileDialog.save(title = "Export Algorithm Settings")
    //      dlg.show(Some(me)).foreach { f =>
    //        SettingsIO.write(settings, f.replaceExt("json"))
    //      }
    //    })
    bindMenu("file.export.table", Action("") {
      val nodes = selectedNodes
      if (nodes.nonEmpty) {
        val dlg = FileDialog.save(title = "Export Selection as PDF Table")
        dlg.show(Some(me)).foreach(f => exportTableAsPDF(f, nodes.map(n => (n.chromosome, n.fitness))))
      }
    })
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
      front()
    }
  }

  def open(): Unit = window.open()

  def exportTableAsPDF(f: File, genome: sys.GenomeVal): Unit = {
    // XXX TODO
    //    import sys.process._
    //    val f1 = f.replaceExt("pdf")
    //    ExportTable(f1, genome, settings)
    //    Seq(pdfViewer, f1.path).!
  }

  class Proc(in: Vec[Node], num: Int) extends ProcessorImpl[Vec[Node], Proc] {
    protected def body(): Vec[Node] = {
      // we want to stop the iteration with evaluation, so that the fitnesses are shown in the top pane
      // ; ensure that initially the nodes have been evaluation
      if (in.exists(_.fitness.isNaN)) stepEval(in)
      checkAborted()
      val out = (in /: (0 until num)) { (itIn, idx) =>
        stepSelect(itIn)
        val itOut = stepBreed(itIn)
        stepEval(itOut)
        val f = (idx + 1).toFloat / num
        progress(f)
        checkAborted()
        itOut
      }
      out
    }
  }
}