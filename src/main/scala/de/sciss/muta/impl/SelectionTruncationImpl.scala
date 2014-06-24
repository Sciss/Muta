/*
 *  SelectionTruncationImpl.scala
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
package impl

/** A truncation type selection algorithm. Cf. http://en.wikipedia.org/wiki/Truncation_selection */
trait SelectionTruncationImpl[Chromosome] extends Selection[Chromosome] {
  def size: SelectionSize

  def apply(pop: Vec[(Chromosome, Double)], r: util.Random): Vec[Chromosome] = {
    val n       = size(pop.size)
    val sorted  = pop.sortBy(_._2)
    sorted.takeRight(n).map(_._1)
  }
}
