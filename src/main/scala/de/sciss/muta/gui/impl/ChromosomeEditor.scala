package de.sciss.muta
package gui
package impl

import javax.swing.JCheckBox
import de.sciss.treetable.j
import scala.swing.event.ValueChanged
import java.awt.event.ActionEvent
import scala.swing.Component

object ChromosomeEditor {
  def apply[A](peer: Component)(getter: => A)(setter: A => Unit): ChromosomeEditor[A] =
    peer match {
      case tf: swing.TextField => new j.DefaultTreeTableCellEditor(tf.peer) with ChromosomeEditor[A] {
        delegate = new EditorDelegate() {
          override def setValue(value: Any): Unit = setter(value.asInstanceOf[A])
          override def getCellEditorValue = getter.asInstanceOf[AnyRef]
        }
      }

      case _ => new j.DefaultTreeTableCellEditor(new JCheckBox()) with ChromosomeEditor[A] {
        override def getTreeTableCellEditorComponent(treeTable: j.TreeTable, value: Any, selected: Boolean,
                                                     row: Int, column: Int): java.awt.Component = {
          // println("Aqui")
          val res       = super.getTreeTableCellEditorComponent(treeTable, value, selected, row, column)
          val renderer  = treeTable.getCellRenderer(row, column)
          renderer.getTreeTableCellRendererComponent(treeTable, value, selected, true, row, column)

          editorComponent.setOpaque(true)
          editorComponent.setBackground(peer.background)
          editorComponent.setBorder(peer.border)

          res
        }

        editorComponent = peer.peer
        peer.reactions += {
          case ValueChanged(_) => delegate.actionPerformed(new ActionEvent(peer.peer, ActionEvent.ACTION_PERFORMED, null))
        }
        peer.peer.setRequestFocusEnabled(false)

        delegate = new EditorDelegate() {
          override def setValue(value: Any): Unit = setter(value.asInstanceOf[A])
          override def getCellEditorValue = getter.asInstanceOf[AnyRef]
        }
      }
    }
}
trait ChromosomeEditor[A] extends j.DefaultTreeTableCellEditor {

  // (getter: => A)(setter: A => Unit)

  override def getTreeTableCellEditorComponent(treeTable: j.TreeTable, value: Any, selected: Boolean,
                                               row: Int, column: Int, expanded: Boolean,
                                               leaf: Boolean): java.awt.Component =
    getTreeTableCellEditorComponent(treeTable, value, selected, row, column)
}