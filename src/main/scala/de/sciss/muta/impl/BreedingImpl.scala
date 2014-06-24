/*
 *  BreedingImpl.scala
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

trait BreedingImpl[Chromosome, Global] extends Breeding[Chromosome, Global] {

  def elitism         : SelectionSize
  def crossoverWeight : SelectionPercent
  def crossover       : BreedingFunction[Chromosome, Global]
  def mutation        : BreedingFunction[Chromosome, Global]

  def apply(g: Vec[(Chromosome, Double, Boolean)], global: Global, r: util.Random): Vec[Chromosome] = {
    val szOut = g.size
    val szEl  = elitism(szOut)
    val out1  = if (szEl == 0) Vec.empty else {
      // ensure that elite choices are distinct (don't want to accumulate five identical chromosomes over time)!
      val eliteCandidates = g.map { case (c, f, _) => (c, f) } .distinct.sortBy(-_._2).map(_._1)
      eliteCandidates.take(szEl)
    }
    val szBr  = szOut - out1.size
    val szX   = crossoverWeight(szBr)
    val szMut = szBr - szX
    val sel   = g.collect {
      case (c, _, true) => c
    }
    val out2  = if (szX   == 0) out1 else out1 ++ crossover(sel, szX  , global, r)
    val out3  = if (szMut == 0) out2 else out2 ++ mutation (sel, szMut, global, r)
    out3
  }
}
