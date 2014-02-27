/*
 *  SelectionRouletteImpl.scala
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
package impl

import scala.annotation.tailrec

/** A roulette type selection algorithm. Cf. http://en.wikipedia.org/wiki/Fitness_proportionate_selection */
trait SelectionRouletteImpl[Chromosome] extends Selection[Chromosome] {
  def size: SelectionSize

  type Genome     = Vec[Chromosome]
  type GenomeVal  = Vec[(Chromosome, Double)]

  override def apply(pop: GenomeVal, r: util.Random): Genome = {
    val n = size(pop.size)

    @tailrec def loop(rem: Int, in: GenomeVal, out: Genome): Genome = if (rem == 0) out else {
      val sum     = in.view.map(_._2).sum
      val rem1    = rem - 1
      if (sum == 0) {
        val ((head, _) +: tail) = in
        loop(rem1, tail, out :+ head)
      } else {
        val norm        = in.zipWithIndex.map { case ((c, f), j) => (j, f / sum) }
        val sorted      = norm.sortBy(_._2)
        val accum       = sorted.scanLeft(0.0) { case (a, (_, f)) => a + f } .tail
        // val max         = accum.last  // ought to be 1.0, but may be slightly off due to floating point noise
        val roul        = r.nextDouble() // * max
        val idxS        = accum.indexWhere(_ > roul)
        val idx         = if (idxS >= 0) sorted(idxS)._1 else in.size - 1
        // println(f"in.size = ${in.size}, accum.size = ${accum.size}, idx = $idx, max $max%1.3f")
        val (chosen, _) = in(idx)
        val in1         = in.patch(idx, Vec.empty, 1) // in.removeAt(idx)
        loop(rem1, in1, out :+ chosen)
      }
    }

    loop(n, pop, Vec.empty)
  }
}
