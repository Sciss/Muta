/*
 *  RatingEditor.scala
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

import javax.swing.JCheckBox
import de.sciss.treetable.j
import scala.swing.event.ValueChanged
import java.awt.event.ActionEvent
import de.sciss.rating.Rating

class RatingEditor(peer: Rating) extends j.DefaultTreeTableCellEditor(new JCheckBox()) {
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

  override def getTreeTableCellEditorComponent(treeTable: j.TreeTable, value: Any, selected: Boolean,
                                               row: Int, column: Int, expanded: Boolean,
                                               leaf: Boolean): java.awt.Component = {
    // val res = super.getTreeTableCellEditorComponent(treeTable, value, selected, row, column, expanded, leaf)
    // res
    getTreeTableCellEditorComponent(treeTable, value, selected, row, column)
  }

  editorComponent = peer.peer
  delegate = new EditorDelegate() {
    override def setValue(value: Any): Unit = {
      peer.value = value match {
        case d: Double => (d * peer.maximum + 0.5).toInt
        case _ => 0
      }
    }

    override def getCellEditorValue = (peer.value.toDouble / peer.maximum).asInstanceOf[AnyRef]
  }
  peer.reactions += {
    case ValueChanged(_) => delegate.actionPerformed(new ActionEvent(peer.peer, ActionEvent.ACTION_PERFORMED, null))
  }
  peer.peer.setRequestFocusEnabled(false)
}