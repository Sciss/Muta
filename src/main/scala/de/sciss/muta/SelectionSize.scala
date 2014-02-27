/*
 *  SelectionSize.scala
 *  (Muta)
 *
 *  Copyright (c) 2013-2014 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU General Public License v2+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.muta

import de.sciss.play.json.AutoFormat

/** Selects an absolute number of individuals
  *
  * @param value  the number of individuals to select
  */
case class SelectionNumber(value: Int = 10) extends SelectionSize {
  require(value > 0)
  override def apply(pop: Int): Int = math.min(pop, value)
}

object SelectionPercent {
  implicit val format = AutoFormat[SelectionPercent]
}
/** Selects the number of individuals corresponding to
  * a given percentage of the total population.
  *
  * @param value  the percentage value ranging from 0 to 100
  */
case class SelectionPercent(value: Int = 20) extends SelectionSize {
  require(value >= 0 && value <= 100)
  override def apply(pop: Int): Int = pop * value / 100
}

object SelectionSize {
  implicit val format = AutoFormat[SelectionSize]
}
sealed trait SelectionSize extends (Int => Int)