//package de.sciss.muta.gui
//
//import java.io.{OutputStream, Writer}
//import java.awt.event.{ActionEvent, MouseEvent, MouseAdapter}
//import javax.swing.{JComponent, JPopupMenu, AbstractAction, JScrollPane, JTextArea, ScrollPaneConstants}
//import java.awt.{GraphicsEnvironment, Font, Color}
//import scala.util.control.NonFatal
//import language.implicitConversions
//
//object LogPane {
//  private val defaultFonts = Seq[(String, Int)](
//    "Menlo"                     -> 12,
//    "DejaVu Sans Mono"          -> 12,
//    "Bitstream Vera Sans Mono"  -> 12,
//    "Monaco"                    -> 12,
//    "Anonymous Pro"             -> 12
//  )
//
//  private def createFont(list: Seq[(String, Int)]): Font = {
//    val allFontNames = GraphicsEnvironment.getLocalGraphicsEnvironment.getAvailableFontFamilyNames
//    val (fontName, fontSize) = list.find(spec => allFontNames.contains(spec._1))
//      .getOrElse("Monospaced" -> 12)
//
//    new Font(fontName, Font.PLAIN, /*if( isMac )*/ fontSize /*else fontSize * 3/4*/)
//  }
//
//  object Settings {
//    implicit def fromBuilder(b: SettingsBuilder): Settings = b.build
//
//    def apply(): SettingsBuilder = new SettingsBuilderImpl
//  }
//
//  sealed trait Settings {
//    def rows      : Int
//    def columns   : Int
//    def background: Color
//    def foreground: Color
//    def font      : Seq[(String, Int)]
//  }
//
//  sealed trait SettingsBuilder extends Settings {
//    var rows      : Int
//    var columns   : Int
//    var background: Color
//    var foreground: Color
//    var font      : Seq[(String, Int)]
//
//    def build: Settings
//  }
//
//  private final class SettingsBuilderImpl extends SettingsBuilder {
//    var rows        = 10
//    var columns     = 60
//    var background  = Color.white
//    var foreground  = Color.black
//    var font        = defaultFonts
//
//    def build: Settings = SettingsImpl(rows, columns, background, foreground, font)
//
//    override def toString = "LogPane.SettingsBuilder@" + hashCode.toHexString
//  }
//
//  private final case class SettingsImpl(rows: Int, columns: Int, background: Color, foreground: Color,
//                                        font: Seq[(String, Int)])
//    extends Settings {
//    override def toString = "LogPane.Settings@" + hashCode.toHexString
//  }
//
//  def apply(settings: Settings = Settings().build): LogPane = new Impl(settings)
//
//  private final class Impl(settings: Settings) extends LogPane {
//    pane =>
//
//    override def toString = "LogPane@" + hashCode.toHexString
//
//    private val textPane: JTextArea = new JTextArea(settings.rows, settings.columns) {
//      me =>
//
//      private var totalLength = 0
//
//      setFont(createFont(settings.font))
//      setEditable(false)
//      setLineWrap(true)
//      setBackground(settings.background) // Color.black )
//      setForeground(settings.foreground) // Color.white )
//      addMouseListener(new MouseAdapter {
//        override def mousePressed (e: MouseEvent): Unit = handleButton(e)
//        override def mouseReleased(e: MouseEvent): Unit = handleButton(e)
//
//        private def handleButton(e: MouseEvent): Unit =
//          if (e.isPopupTrigger) {
//            popup.show(me, e.getX, e.getY)
//          }
//      })
//
//      override def append(str: String): Unit = {
//        super.append(str)
//        totalLength += str.length
//        updateCaret()
//      }
//
//      override def setText(str: String): Unit = {
//        super.setText(str)
//        totalLength = if (str == null) 0 else str.length
//      }
//
//      private def updateCaret(): Unit =
//        try {
//          setCaretPosition(math.max(0, totalLength - 1))
//        }
//        catch {
//          case NonFatal(_) => /* ignore */
//        }
//    }
//
//    // ---- Writer ----
//    val writer: Writer = new Writer {
//      override def toString = pane.toString + ".writer"
//
//      def close() = ()
//
//      def flush() = ()
//
//      def write(ch: Array[Char], off: Int, len: Int): Unit = {
//        val str = new String(ch, off, len)
//        textPane.append(str)
//      }
//    }
//
//    // ---- OutputStream ----
//    val outputStream: OutputStream = new OutputStream {
//      override def toString = pane.toString + ".outputStream"
//
//      override def write(b: Array[Byte], off: Int, len: Int): Unit = {
//        val str = new String(b, off, len)
//        textPane.append(str)
//      }
//
//      def write(b: Int): Unit = write(Array(b.toByte), 0, 1)
//    }
//
//    val component = new JScrollPane(textPane,
//      ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
//      ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)
//
//    private val popup = {
//      val p = new JPopupMenu()
//      p.add(new AbstractAction("Clear All") {
//        override def actionPerformed(e: ActionEvent): Unit = clear()
//      })
//      p
//    }
//
//    def clear(): Unit = textPane.setText(null)
//
//    def makeDefault(error: Boolean): LogPane = {
//      Console.setOut(outputStream)
//      if (error) Console.setErr(outputStream)
//      this
//    }
//  }
//}
//
///** A pane widget which can be used to log text output, and which can be hooked up to capture the
//  * default console output.
//  */
//trait LogPane {
//  /** The Swing component which can be added to a Swing parent container. */
//  def component: JComponent
//
//  /** A `Writer` which will write to the pane. */
//  def writer: Writer
//
//  /** An `OutputStream` which will write to the pane. */
//  def outputStream: OutputStream
//
//  /** Clears the contents of the pane. */
//  def clear(): Unit
//
//  /** Make this log pane the default text output for
//    * `Console.out` and optionally for `Console.err` as well.
//    *
//    * @return  the method returns the log pane itself for convenience and method concatenation
//    */
//  def makeDefault(error: Boolean = true): LogPane
//}